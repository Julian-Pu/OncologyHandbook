package com.oncology.handbook.util

import android.content.Context
import android.content.SharedPreferences
import com.oncology.handbook.App
import com.oncology.handbook.data.entity.UserSection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 手册内容仓库
 * 合并内置章节（ManualContent硬编码）与用户新增章节（数据库），
 * 过滤用户删除的内置章节（SharedPreferences标记）
 */
object ManualRepository {

    private const val PREFS_NAME = "manual_prefs"
    private const val KEY_DELETED_SECTIONS = "deleted_builtin_sections"
    private const val KEY_DELETED_CATEGORIES = "deleted_builtin_categories"

    /** 用户章节ID前缀，用于与内置章节区分 */
    const val USER_SECTION_PREFIX = "user_"

    /** 用户分类ID前缀 */
    const val USER_CATEGORY_PREFIX = "usercat_"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** 判断是否为用户新增章节 */
    fun isUserSection(sectionId: String): Boolean {
        return sectionId.startsWith(USER_SECTION_PREFIX)
    }

    /** 从用户章节ID中提取数据库主键 */
    fun getUserSectionDbId(sectionId: String): Long {
        return sectionId.removePrefix(USER_SECTION_PREFIX).toLongOrNull() ?: -1L
    }

    /** 构建用户章节的显示ID */
    fun buildUserSectionId(dbId: Long): String {
        return "$USER_SECTION_PREFIX$dbId"
    }

    /** 判断是否为用户新增分类 */
    fun isUserCategory(categoryId: String): Boolean {
        return categoryId.startsWith(USER_CATEGORY_PREFIX)
    }

    /** 从用户分类ID中提取数据库主键 */
    fun getUserCategoryDbId(categoryId: String): Long {
        return categoryId.removePrefix(USER_CATEGORY_PREFIX).toLongOrNull() ?: -1L
    }

    /** 构建用户分类的显示ID */
    fun buildUserCategoryId(dbId: Long): String {
        return "$USER_CATEGORY_PREFIX$dbId"
    }

    /** 获取已删除的内置章节ID集合 */
    private fun getDeletedSectionIds(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_DELETED_SECTIONS, emptySet()) ?: emptySet()
    }

    /** 标记内置章节为已删除 */
    fun markBuiltInSectionDeleted(context: Context, sectionId: String) {
        val prefs = getPrefs(context)
        val deleted = prefs.getStringSet(KEY_DELETED_SECTIONS, emptySet())?.toMutableSet() ?: mutableSetOf()
        deleted.add(sectionId)
        prefs.edit().putStringSet(KEY_DELETED_SECTIONS, deleted).apply()
    }

    /** 判断内置章节是否已被删除 */
    fun isBuiltInSectionDeleted(context: Context, sectionId: String): Boolean {
        return getDeletedSectionIds(context).contains(sectionId)
    }

    /** 获取已删除的内置分类ID集合 */
    private fun getDeletedCategoryIds(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_DELETED_CATEGORIES, emptySet()) ?: emptySet()
    }

    /** 标记内置分类为已删除 */
    fun markBuiltInCategoryDeleted(context: Context, categoryId: String) {
        val prefs = getPrefs(context)
        val deleted = prefs.getStringSet(KEY_DELETED_CATEGORIES, emptySet())?.toMutableSet() ?: mutableSetOf()
        deleted.add(categoryId)
        prefs.edit().putStringSet(KEY_DELETED_CATEGORIES, deleted).apply()
    }

    /** 判断内置分类是否已被删除 */
    fun isBuiltInCategoryDeleted(context: Context, categoryId: String): Boolean {
        return getDeletedCategoryIds(context).contains(categoryId)
    }

    /**
     * 获取合并后的分类列表（含内置分类+用户新增分类，过滤已删除）
     */
    suspend fun getCategories(context: Context): List<ManualCategory> = withContext(Dispatchers.IO) {
        val deletedSectionIds = getDeletedSectionIds(context)
        val deletedCategoryIds = getDeletedCategoryIds(context)
        val userSections = App.instance.database.userSectionDao().getAll()
        val userCategories = App.instance.database.userCategoryDao().getAll()

        // 按categoryId分组用户章节
        val userSectionsByCategory = userSections.groupBy { it.categoryId }

        // 内置分类：过滤已删除的分类和章节，追加用户章节
        val builtInCategories = ManualContent.categories
            .filter { !deletedCategoryIds.contains(it.id) }
            .map { category ->
                val builtInSections = category.sections.filter { !deletedSectionIds.contains(it.id) }
                val userSectionList = userSectionsByCategory[category.id]?.map { userSection ->
                    ManualSection(
                        id = buildUserSectionId(userSection.id),
                        title = userSection.title,
                        htmlContent = userSection.htmlContent
                    )
                } ?: emptyList()
                category.copy(sections = builtInSections + userSectionList)
            }

        // 用户新增分类
        val userCategoryList = userCategories.map { userCat ->
            val catId = buildUserCategoryId(userCat.id)
            val sections = userSectionsByCategory[catId]?.map { userSection ->
                ManualSection(
                    id = buildUserSectionId(userSection.id),
                    title = userSection.title,
                    htmlContent = userSection.htmlContent
                )
            } ?: emptyList()
            ManualCategory(
                id = catId,
                title = userCat.title,
                icon = userCat.icon,
                sections = sections
            )
        }

        (builtInCategories + userCategoryList).filter { it.sections.isNotEmpty() || isUserCategory(it.id) }
    }

    /**
     * 查找章节（先查内置，再查用户新增）
     */
    suspend fun findSection(context: Context, categoryId: String, sectionId: String): ManualSection? {
        if (isUserSection(sectionId)) {
            val dbId = getUserSectionDbId(sectionId)
            if (dbId > 0) {
                val userSection = withContext(Dispatchers.IO) {
                    App.instance.database.userSectionDao().getById(dbId)
                }
                if (userSection != null) {
                    return ManualSection(
                        id = sectionId,
                        title = userSection.title,
                        htmlContent = userSection.htmlContent
                    )
                }
            }
            return null
        }
        // 内置章节：检查是否被删除
        if (isBuiltInSectionDeleted(context, sectionId)) return null
        return ManualContent.findSection(categoryId, sectionId)
    }

    /**
     * 新增用户章节，返回数据库主键ID
     */
    suspend fun addUserSection(context: Context, categoryId: String, title: String): Long {
        return withContext(Dispatchers.IO) {
            val userSection = UserSection(
                categoryId = categoryId,
                title = title,
                htmlContent = "<p>在此输入内容...</p>",
                orderIndex = Int.MAX_VALUE // 排在最后
            )
            App.instance.database.userSectionDao().insert(userSection)
        }
    }

    /**
     * 更新用户章节内容
     */
    suspend fun updateUserSectionContent(context: Context, sectionId: String, htmlContent: String) {
        if (!isUserSection(sectionId)) return
        val dbId = getUserSectionDbId(sectionId)
        if (dbId <= 0) return
        withContext(Dispatchers.IO) {
            val dao = App.instance.database.userSectionDao()
            dao.getById(dbId)?.let { existing ->
                dao.update(existing.copy(htmlContent = htmlContent, updatedAt = System.currentTimeMillis()))
            }
        }
    }

    /**
     * 删除用户章节
     */
    suspend fun deleteUserSection(context: Context, sectionId: String) {
        if (!isUserSection(sectionId)) return
        val dbId = getUserSectionDbId(sectionId)
        if (dbId <= 0) return
        withContext(Dispatchers.IO) {
            App.instance.database.userSectionDao().getById(dbId)?.let {
                App.instance.database.userSectionDao().delete(it)
            }
        }
    }

    /**
     * 新增用户分类，返回数据库主键ID
     */
    suspend fun addUserCategory(context: Context, title: String): Long {
        return withContext(Dispatchers.IO) {
            val userCategory = com.oncology.handbook.data.entity.UserCategory(
                title = title,
                icon = "folder",
                orderIndex = Int.MAX_VALUE
            )
            App.instance.database.userCategoryDao().insert(userCategory)
        }
    }

    /**
     * 删除用户分类及其下所有用户章节
     */
    suspend fun deleteUserCategory(context: Context, categoryId: String) {
        if (!isUserCategory(categoryId)) return
        val dbId = getUserCategoryDbId(categoryId)
        if (dbId <= 0) return
        withContext(Dispatchers.IO) {
            val dao = App.instance.database.userCategoryDao()
            dao.getById(dbId)?.let { dao.delete(it) }
            // 级联删除该分类下的所有用户章节
            val sections = App.instance.database.userSectionDao().getByCategoryId(categoryId)
            sections.forEach { App.instance.database.userSectionDao().delete(it) }
        }
    }

    /**
     * 搜索章节（合并内置+用户）
     */
    suspend fun search(context: Context, keyword: String): List<Pair<ManualCategory, ManualSection>> {
        val allCategories = getCategories(context)
        val results = mutableListOf<Pair<ManualCategory, ManualSection>>()
        for (cat in allCategories) {
            for (sec in cat.sections) {
                if (sec.title.contains(keyword) || sec.htmlContent.contains(keyword)) {
                    results.add(cat to sec)
                }
            }
        }
        return results
    }
}
