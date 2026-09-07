package com.oncology.handbook.util

/**
 * 内置手册内容数据结构
 * 内容来源于肿瘤科医生值班手册大纲
 */
data class ManualSection(
    val id: String,
    val title: String,
    val htmlContent: String
)

data class ManualCategory(
    val id: String,
    val title: String,
    val icon: String, // 图标标识
    val sections: List<ManualSection>
)

object ManualContent {

    val categories: List<ManualCategory> = listOf(
        // ========== 第一部分：值班手册 ==========
        ManualCategory(
            id = "duty",
            title = "值班手册",
            icon = "stethoscope",
            sections = listOf(
                ManualSection(
                    id = "duty_prep",
                    title = "一、值班前准备",
                    htmlContent = """
                    <h3>1. 熟悉病区情况</h3>
                    <p>接班前应巡视病房，了解全病区患者情况，重点掌握<strong>危重患者、新入院患者及病情不稳定患者</strong>的详细信息。</p>
                    <h3>2. 交接班要点</h3>
                    <ul>
                    <li>接班者提前10分钟到科室进行当面交接班</li>
                    <li>危重患者需进行床旁交接</li>
                    <li>交班内容应包括：患者病情变化、治疗方案调整、特殊医嘱、需重点关注的问题等</li>
                    <li>遵守<strong>"接班不到，当班不走"</strong>的原则，危重患者处于危险中时不应交接班，应协同处理直至病情稳定</li>
                    </ul>
                    <p class="tip">参考：北京大学肿瘤医院三基学习平台——值班和交接班制度</p>
                    """.trimIndent()
                ),
                ManualSection(
                    id = "duty_symptoms",
                    title = "二、值班常见症状及处理",
                    htmlContent = """
                    <h3>1. 发热</h3>
                    <ul>
                    <li>首先鉴别<strong>感染性发热与肿瘤热</strong></li>
                    <li>注意患者有无PICC、CVC等管路</li>
                    <li>完善血常规、降钙素原、血培养等检查</li>
                    <li>体温＞38.3℃持续1小时以上，或＞38.0℃持续1小时以上伴中性粒细胞减少，按<strong>发热性中性粒细胞减少症（FN）</strong>处理</li>
                    </ul>
                    <h3>2. 疼痛</h3>
                    <ul>
                    <li>常规评估疼痛病情，相关记录应在患者入院后8小时内完成</li>
                    <li>根据<strong>WHO三阶梯镇痛原则</strong>予以镇痛</li>
                    <li>动态评估对药物止痛治疗的剂量滴定至关重要</li>
                    <li>鉴别疼痛原因，排除病理性骨折、脑转移、感染、肠梗阻等急症</li>
                    </ul>
                    <h3>3. 恶心呕吐</h3>
                    <ul>
                    <li>评估呕吐原因（化疗相关、颅内压增高、肠梗阻等）</li>
                    <li>化疗所致恶心呕吐参照止吐指南进行分级预防和治疗</li>
                    </ul>
                    <h3>4. 腹泻与便秘</h3>
                    <ul>
                    <li><strong>腹泻</strong>：评估脱水程度，补液，考虑止泻药物，排查感染性肠炎</li>
                    <li><strong>便秘</strong>：评估有无肠梗阻，使用缓泻剂</li>
                    </ul>
                    <h3>5. 出血（咯血、消化道出血等）</h3>
                    <ul>
                    <li><strong>鼻咽大出血</strong>：取侧卧位，防止血液吸入气管，安慰患者，吸取积血，后鼻孔填塞，使用抗生素及输血，暂停进食，静脉输液</li>
                    <li><strong>大咯血</strong>处理同上</li>
                    <li><strong>消化道大出血</strong>：禁食、补液、止血药物，必要时输血</li>
                    </ul>
                    <p class="tip">参考：四川省肿瘤医院——肿瘤急症及处理；必备宝典：肿瘤科值班常见症状及处理</p>
                    """.trimIndent()
                ),
                ManualSection(
                    id = "duty_emergency",
                    title = "三、常见肿瘤高危急症处理",
                    htmlContent = """
                    <h3>1. 上腔静脉综合征（SVCS）</h3>
                    <p><strong>快速识别</strong>：呼吸困难、面颈肿胀、上肢肿胀，严重时伴颈静脉曲张和胸壁静脉曲张</p>
                    <p><strong>紧急处理</strong>：</p>
                    <ul>
                    <li>一般处理：卧床休息，头抬高，吸氧，下肢静脉补液</li>
                    <li>药物治疗：地塞米松 20-40mg IV</li>
                    <li>利尿剂应用</li>
                    <li>病因治疗：放疗科急会诊（3-4Gy/d起始），化疗敏感者即刻化疗，必要时介入科支架置入</li>
                    </ul>

                    <h3>2. 肿瘤溶解综合征（TLS）</h3>
                    <p><strong>快速识别</strong>：常见于化疗或放疗后，表现为<strong>高钾血症、高磷血症、高尿酸血症和急性肾损伤</strong></p>
                    <p><strong>紧急检查</strong>：血电解质、肾功能、尿酸水平</p>
                    <p><strong>处理流程</strong>：</p>
                    <ul>
                    <li>预防：高危患者化疗前给予别嘌呤醇或拉布立酶</li>
                    <li>水化治疗：充分补液，尿液保持在2000ml/日以上</li>
                    <li>纠正电解质紊乱</li>
                    <li>严重肾损伤时考虑透析</li>
                    </ul>

                    <h3>3. 高钙血症</h3>
                    <p><strong>快速识别</strong>：乏力、恶心、呕吐、多尿、意识模糊，严重时可致心律失常</p>
                    <p><strong>处理流程</strong>：</p>
                    <ul>
                    <li>水化利尿：静脉输入生理盐水</li>
                    <li>药物治疗：双膦酸盐或RANK配体抑制剂</li>
                    <li>透析治疗：不能耐受大量输液者</li>
                    </ul>

                    <h3>4. 脊髓压迫</h3>
                    <p><strong>快速识别</strong>：剧烈背痛、肢体麻木、大小便失禁，常见于乳腺癌、肺癌、前列腺癌转移</p>
                    <p><strong>紧急检查</strong>：全脊柱MRI</p>
                    <p><strong>处理流程</strong>：</p>
                    <ul>
                    <li>糖皮质激素减轻脊髓水肿</li>
                    <li>放疗（30-40Gy/10-20f）</li>
                    <li>必要时手术</li>
                    </ul>

                    <h3>5. 颅内压增高</h3>
                    <p><strong>快速识别</strong>：头痛、呕吐、视神经乳头水肿</p>
                    <p><strong>紧急检查</strong>：增强MRI</p>
                    <p><strong>处理流程</strong>：</p>
                    <ul>
                    <li>激素、脱水（甘露醇）、镇静止痛、营养神经</li>
                    <li>放疗：30Gy/10f 或 37.5Gy/15f</li>
                    </ul>

                    <h3>6. 恶性心包积液/心脏压塞</h3>
                    <p><strong>快速识别</strong>：呼吸困难、颈静脉怒张、低血压、奇脉</p>
                    <p><strong>处理流程</strong>：心包穿刺引流，病因治疗</p>
                    <p class="tip">参考：肿瘤急症值班手册：六大高危状况识别与紧急处置流程；四川省肿瘤医院——肿瘤急症及处理</p>
                    """.trimIndent()
                ),
                ManualSection(
                    id = "duty_adverse",
                    title = "四、抗肿瘤治疗相关不良反应处理",
                    htmlContent = """
                    <h3>1. 中性粒细胞减少症及发热性中性粒细胞减少（FN）</h3>
                    <ul>
                    <li>所有抗肿瘤药物均可导致骨髓抑制</li>
                    <li>FN高风险化疗方案必须进行<strong>一级预防</strong></li>
                    <li>FN患者需立即经验性使用<strong>抗假单胞菌β-内酰胺类抗生素单药治疗</strong></li>
                    </ul>

                    <h3>2. 血小板减少症</h3>
                    <ul>
                    <li>监测血小板计数</li>
                    <li>根据CSCO肿瘤治疗所致血小板减少症诊疗指南进行分级管理</li>
                    <li>新型TPO-RA如海曲泊帕可用于治疗和二级预防</li>
                    </ul>

                    <h3>3. 心脏毒性</h3>
                    <p>肿瘤治疗相关心功能不全（CTRCD）最为常见</p>
                    <ul>
                    <li><strong>轻度CTRCD</strong>（无症状）：严密监测</li>
                    <li><strong>中度CTRCD</strong>（LVEF 40%-49%）：暂停治疗并启动心脏保护治疗（ACEI/ARB、β受体阻滞剂）</li>
                    <li><strong>严重CTRCD</strong>（急性心肌炎、严重心衰）：立即停止抗肿瘤治疗，转入ICU</li>
                    </ul>

                    <h3>4. 肝损伤</h3>
                    <ul>
                    <li>抗肿瘤药物相关性肝损伤需及时识别和处理</li>
                    <li>根据2025版CSCO指南进行分级管理和治疗</li>
                    </ul>
                    <p class="tip">参考：CSCO抗肿瘤治疗所致中性粒细胞减少症诊断、预防和治疗指南（2025）；CSCO肿瘤心脏病学临床实践指南2025</p>
                    """.trimIndent()
                ),
                ManualSection(
                    id = "duty_pain",
                    title = "五、癌痛规范化管理",
                    htmlContent = """
                    <ul>
                    <li>主动询问癌症患者有无疼痛</li>
                    <li>鉴别疼痛原因，排除需特殊处理的急症</li>
                    <li>根据<strong>WHO三阶梯镇痛原则</strong></li>
                    <li>对于急性、重度疼痛或疼痛危象，考虑紧急评估和住院治疗</li>
                    </ul>
                    <h4>WHO三阶梯镇痛原则</h4>
                    <ol>
                    <li><strong>第一阶梯</strong>：非阿片类药物（如对乙酰氨基酚、NSAIDs），用于轻度疼痛</li>
                    <li><strong>第二阶梯</strong>：弱阿片类药物（如可待因、曲马多），用于中度疼痛</li>
                    <li><strong>第三阶梯</strong>：强阿片类药物（如吗啡、羟考酮、芬太尼），用于重度疼痛</li>
                    </ol>
                    <p>辅助用药：抗惊厥药（加巴喷丁、普瑞巴林）用于神经病理性疼痛；抗抑郁药（阿米替林、度洛西汀）；糖皮质激素用于炎症性疼痛和颅内压增高。</p>
                    <p class="tip">参考：NCCN成人癌痛指南2025中文版；CSCO癌症疼痛诊疗上海专家共识</p>
                    """.trimIndent()
                ),
                ManualSection(
                    id = "duty_palliative",
                    title = "六、临终关怀与姑息治疗",
                    htmlContent = """
                    <ul>
                    <li>晚期肿瘤患者常伴有多种并发症，需全面评估</li>
                    <li>关注患者疼痛、呼吸困难、恶心呕吐等症状的姑息处理</li>
                    <li>及时与患者及家属沟通病情，明确治疗目标</li>
                    </ul>
                    <h4>常见症状姑息处理要点</h4>
                    <p><strong>呼吸困难</strong>：端坐位、吸氧、阿片类药物（吗啡）减轻呼吸困难感、抗焦虑药物、 Fans 吹拂面部</p>
                    <p><strong>恶心呕吐</strong>：氟哌啶醇、甲氧氯普胺、昂丹司琼；肠梗阻时使用生长抑素类似物</p>
                    <p><strong>谵妄</strong>：氟哌啶醇、奥氮平；纠正可逆因素（感染、便秘、尿潴留、药物）</p>
                    <p><strong>口腔问题</strong>：口腔护理、人工唾液、抗真菌治疗</p>
                    <p class="tip">参考：晚期肿瘤相关急症处理</p>
                    """.trimIndent()
                ),
                ManualSection(
                    id = "duty_drugs",
                    title = "七、值班常用抢救药物参考",
                    htmlContent = """
                    <table border="1" cellpadding="8" cellspacing="0" style="border-collapse:collapse;width:100%;">
                    <tr style="background-color:#e3f2fd;"><th>药物</th><th>常用剂量</th><th>适应症</th></tr>
                    <tr><td><strong>甘露醇</strong></td><td>0.25-1g/kg，30-60min静滴</td><td>脑水肿、颅内高压</td></tr>
                    <tr><td><strong>地塞米松</strong></td><td>10-40mg IV bolus</td><td>脊髓压迫、SVCS、脑水肿</td></tr>
                    <tr><td><strong>咪达唑仑</strong></td><td>1-5mg IV</td><td>镇静、癫痫持续状态</td></tr>
                    <tr><td><strong>头孢吡肟</strong></td><td>2g IV q8h</td><td>FN经验性抗感染</td></tr>
                    <tr><td><strong>万古霉素</strong></td><td>15-20mg/kg IV q8-12h</td><td>FN经验性抗感染（革兰阳性菌）</td></tr>
                    <tr><td><strong>呋塞米</strong></td><td>20-40mg IV</td><td>心衰、容量过负荷、高钙血症辅助</td></tr>
                    <tr><td><strong>奥美拉唑</strong></td><td>40mg IV q12h</td><td>消化道出血、应激性溃疡</td></tr>
                    <tr><td><strong>生长抑素</strong></td><td>250μg 负荷，250μg/h 维持</td><td>消化道大出血</td></tr>
                    <tr><td><strong>胺碘酮</strong></td><td>150mg IV 10min，后1mg/min维持</td><td>室性心动过速、房颤</td></tr>
                    <tr><td><strong>葡萄糖酸钙</strong></td><td>1g IV 缓慢</td><td>高钾血症、低钙血症</td></tr>
                    </table>
                    <p class="tip">参考：有备无患，抢救药物使用一表牢记；Oncologic Emergencies - EB Medicine</p>
                    """.trimIndent()
                ),
                ManualSection(
                    id = "duty_reference",
                    title = "八、综合参考资源",
                    htmlContent = """
                    <h3>指南与手册</h3>
                    <ol>
                    <li>《肿瘤急症诊疗备忘录》（彭玲，2025）- 科学出版社</li>
                    <li>《肿瘤内科医嘱速查手册（第2版）》- 化学工业出版社</li>
                    <li>《西京肿瘤科临床工作手册》- 第四军医大学出版社</li>
                    <li>《肿瘤内科医师查房手册（第2版）》- 2025</li>
                    <li>《牛津肿瘤学手册（第5版）》- 2025</li>
                    </ol>
                    <h3>主要学会指南</h3>
                    <ul>
                    <li><strong>CSCO指南</strong>：中性粒细胞减少症、血小板减少症、恶心呕吐、肝损伤、心脏病学等系列指南</li>
                    <li><strong>NCCN指南</strong>：止吐、癌痛等</li>
                    <li><strong>ACC指南</strong>：肿瘤治疗心血管不良反应</li>
                    </ul>
                    <h3>在线学习平台</h3>
                    <p>北京大学肿瘤医院三基学习平台</p>
                    <hr/>
                    <p class="warning"><strong>提示</strong>：本手册为值班参考工具，具体诊疗决策应结合患者个体情况、最新指南及本院制度执行。建议定期更新指南版本，并将本手册与医院HIS系统、药典等工具配合使用。</p>
                    """.trimIndent()
                )
            )
        ),

        // ========== 第二部分：心电图学习手册 ==========
        ManualCategory(
            id = "ecg",
            title = "心电图学习",
            icon = "heart",
            sections = listOf(
                ManualSection(
                    id = "ecg_why",
                    title = "一、肿瘤科医生为何需要掌握心电图",
                    htmlContent = """
                    <p>肿瘤治疗相关心血管毒性日益受到重视。抗肿瘤药物（如<strong>蒽环类药物、HER2靶向药物、免疫检查点抑制剂、VEGF抑制剂</strong>等）可导致心肌损伤、心律失常、QT间期延长、高血压、心力衰竭等多种心脏不良反应。</p>
                    <p>所有存在心血管危险因素或接受心脏毒性抗肿瘤药物的患者，均应进行<strong>基线及规律随访的12导联心电图检查</strong>。</p>
                    <h4>肿瘤科值班常见心电图相关场景</h4>
                    <ul>
                    <li>化疗后心悸、胸闷患者的心电图判读</li>
                    <li>靶向/免疫治疗期间新发心律失常的识别</li>
                    <li>电解质紊乱（尤其化疗后呕吐、腹泻所致）的心电图表现</li>
                    <li>肿瘤急症（如高钙血症、低镁血症）的心电图改变</li>
                    <li>临终患者心电图变化的识别</li>
                    </ul>
                    """.trimIndent()
                ),
                ManualSection(
                    id = "ecg_path",
                    title = "二、心电图学习路径",
                    htmlContent = """
                    <h3>第一阶段：夯实基础（1-2周）</h3>
                    <h4>1. 心脏解剖与电生理基础</h4>
                    <ul>
                    <li>心脏传导系统：窦房结→房室结→希氏束→左右束支→浦肯野纤维</li>
                    <li>动作电位与心电图波形的关系</li>
                    <li>心电向量与导联原理</li>
                    </ul>
                    <h4>2. 心电图导联系统</h4>
                    <ul>
                    <li>标准12导联：肢体导联（Ⅰ、Ⅱ、Ⅲ、aVR、aVL、aVF）+ 胸导联（V1-V6）</li>
                    <li>Einthoven三角与额面六轴导联系统</li>
                    <li>胸导联QRS波正常演变规律</li>
                    </ul>
                    <h4>3. 正常心电图各波段</h4>
                    <table border="1" cellpadding="6" cellspacing="0" style="border-collapse:collapse;width:100%;">
                    <tr style="background-color:#e3f2fd;"><th>波段</th><th>意义</th><th>正常范围</th></tr>
                    <tr><td><strong>P波</strong></td><td>心房除极</td><td>时限≤0.11s，振幅≤0.25mV</td></tr>
                    <tr><td><strong>PR间期</strong></td><td>心房开始除极至心室开始除极</td><td>0.12-0.20s</td></tr>
                    <tr><td><strong>QRS波</strong></td><td>心室除极</td><td>时限0.06-0.10s</td></tr>
                    <tr><td><strong>ST段</strong></td><td>心室缓慢复极期</td><td>与基线平齐</td></tr>
                    <tr><td><strong>T波</strong></td><td>心室快速复极</td><td>与主波方向一致</td></tr>
                    <tr><td><strong>QT间期</strong></td><td>心室除极和复极全过程</td><td>需根据心率校正（QTc）</td></tr>
                    </table>

                    <h3>第二阶段：掌握标准化判读流程（2-3周）</h3>
                    <p><strong>系统性分析步骤口诀："律和率、轴和肌，房室传导波段期"</strong></p>
                    <table border="1" cellpadding="6" cellspacing="0" style="border-collapse:collapse;width:100%;">
                    <tr style="background-color:#e3f2fd;"><th>步骤</th><th>内容</th><th>要点</th></tr>
                    <tr><td>1. 律</td><td>心律</td><td>是否为窦性心律？P波形态、方向</td></tr>
                    <tr><td>2. 率</td><td>心率</td><td>300-150-100-75-60-50法或300÷RR间期</td></tr>
                    <tr><td>3. 轴</td><td>心电轴</td><td>Ⅰ、Ⅲ导联QRS主波方向判断</td></tr>
                    <tr><td>4. 肌</td><td>心肌肥厚</td><td>心房肥大、心室肥厚的心电图标准</td></tr>
                    <tr><td>5. 房室</td><td>房室传导</td><td>PR间期、QRS时限</td></tr>
                    <tr><td>6. 波段</td><td>各波段形态</td><td>P波、QRS波、ST段、T波、QT间期</td></tr>
                    </table>
                    <p><strong>定准电压与走纸速度确认</strong>：分析前首先确认心电图记录的定准电压（标准1mV）和走纸速度（标准25mm/s），以免误诊或漏诊。</p>

                    <h3>第三阶段：常见异常心电图识别（4-6周）</h3>
                    <h4>1. 心律失常</h4>
                    <ul>
                    <li>窦性心律失常（心动过速、心动过缓、不齐、停搏）</li>
                    <li>期前收缩（房性、交界性、室性）</li>
                    <li>心动过速（室上性、室性）</li>
                    <li>心房扑动与心房颤动</li>
                    <li>房室传导阻滞（一度、二度Ⅰ型/Ⅱ型、三度）</li>
                    </ul>
                    <h4>2. 心肌缺血与心肌梗死</h4>
                    <ul>
                    <li>心肌缺血：ST段水平型或下斜型压低，T波倒置</li>
                    <li>心肌损伤：ST段抬高</li>
                    <li>心肌梗死：病理性Q波+ST-T动态演变</li>
                    <li>梗死定位诊断（前壁、下壁、侧壁、后壁等）</li>
                    </ul>
                    <h4>3. 心房与心室肥大</h4>
                    <ul>
                    <li>左心房肥大：P波增宽、双峰</li>
                    <li>右心房肥大：P波高尖（肺性P波）</li>
                    <li>左心室肥厚：左室高电压+ST-T改变</li>
                    <li>右心室肥厚：右室高电压+电轴右偏</li>
                    </ul>
                    <h4>4. 电解质紊乱</h4>
                    <ul>
                    <li><strong>高钾血症</strong>：T波高尖、QRS增宽</li>
                    <li><strong>低钾血症</strong>：T波低平、U波明显</li>
                    <li><strong>低钙血症</strong>：ST段延长、QT间期延长</li>
                    <li><strong>高钙血症</strong>：ST段缩短、QT间期缩短</li>
                    </ul>
                    <h4>5. 药物相关心电图改变</h4>
                    <ul>
                    <li>QT间期延长（多种抗肿瘤药物）</li>
                    <li>心律失常（免疫检查点抑制剂相关心肌炎）</li>
                    </ul>
                    """.trimIndent()
                ),
                ManualSection(
                    id = "ecg_scenarios",
                    title = "三、肿瘤科值班重点关注的ECG场景",
                    htmlContent = """
                    <h3>1. QT间期延长</h3>
                    <p><strong>常见致QT延长的抗肿瘤药物</strong>：三氧化二砷、酪氨酸激酶抑制剂（如舒尼替尼、索拉非尼）、HDAC抑制剂、部分抗雌激素药物等。</p>
                    <p><strong>值班处理要点</strong>：</p>
                    <ul>
                    <li>QTc＞450ms（男性）或＞460ms（女性）应警惕</li>
                    <li>QTc＞500ms或较基线延长＞60ms，需暂停相关药物</li>
                    <li>排查电解质（尤其K⁺、Mg²⁺），及时纠正</li>
                    <li>避免联合使用其他致QT延长药物</li>
                    </ul>

                    <h3>2. 免疫检查点抑制剂相关心肌炎</h3>
                    <p><strong>心电图表现</strong>：可表现为多种心律失常（<strong>房室传导阻滞最常见</strong>）、ST-T改变、低电压等，心电图异常可早于临床症状出现。</p>
                    <p><strong>值班处理要点</strong>：</p>
                    <ul>
                    <li>新发心悸、胸闷、气短的患者立即行心电图</li>
                    <li>发现传导阻滞或心律失常，需紧急评估心肌酶（肌钙蛋白）</li>
                    <li>高度怀疑者请心内科急会诊</li>
                    <li>早期应用糖皮质激素</li>
                    </ul>

                    <h3>3. 蒽环类药物心脏毒性</h3>
                    <p><strong>心电图表现</strong>：非特异性ST-T改变、QRS低电压、QT间期延长、室性早搏等。</p>
                    <p><strong>值班处理要点</strong>：</p>
                    <ul>
                    <li>化疗期间新发心电图异常需评估心功能（超声心动图）</li>
                    <li>监测累积剂量（阿霉素≤450-550mg/m²）</li>
                    </ul>

                    <h3>4. 肿瘤溶解综合征相关心电图改变</h3>
                    <p><strong>机制</strong>：高钾血症、低钙血症、高尿酸血症</p>
                    <p><strong>心电图表现</strong>：</p>
                    <ul>
                    <li><strong>高钾血症</strong>：T波高尖、QRS增宽、P波消失（需紧急处理）</li>
                    <li><strong>低钙血症</strong>：ST段延长、QT间期延长</li>
                    </ul>

                    <h3>5. 高钙血症相关心电图</h3>
                    <p><strong>心电图表现</strong>：ST段缩短、QT间期缩短、QRS增宽，严重时可致心律失常。</p>
                    """.trimIndent()
                ),
                ManualSection(
                    id = "ecg_resources",
                    title = "四、推荐学习资源",
                    htmlContent = """
                    <h3>推荐书籍</h3>
                    <table border="1" cellpadding="6" cellspacing="0" style="border-collapse:collapse;width:100%;">
                    <tr style="background-color:#e3f2fd;"><th>书名</th><th>特点</th><th>适合阶段</th></tr>
                    <tr><td>《彩色简明心电图手册》（第二版）</td><td>600余幅彩图，系统阐述基础、导联、术语及各疾病心电图特征</td><td>入门-进阶</td></tr>
                    <tr><td>《医学规培生应知应会心电图快速判读手册》</td><td>以图谱引出判读步骤，每张图按统一路径分析</td><td>入门</td></tr>
                    <tr><td>《临床心电图分析与诊断》（第3版）</td><td>从基础知识到各类异常心电图，配有高质量图谱</td><td>系统学习</td></tr>
                    <tr><td>《肿瘤心脏病学心电病例集》</td><td>38个肿瘤心脏病学临床病例，突出心电图在抗肿瘤药物心脏毒性监测中的作用</td><td>肿瘤专科进阶</td></tr>
                    </table>
                    <h3>在线课程与平台</h3>
                    <ul>
                    <li><strong>国家高等教育智慧教育平台</strong>：心电图课程，含心肌缺血与心肌梗死、心律失常等内容</li>
                    <li><strong>UpToDate</strong>：心电图教程——心肌缺血和心肌梗死</li>
                    </ul>
                    <h3>实用记忆口诀</h3>
                    <p><strong>"律和率、轴和肌，房室传导波段期"</strong> ——系统性判读心电图的步骤口诀</p>
                    <p><strong>心率速算法</strong>：300-150-100-75-60-50-43-37-33-30（相邻RR间期大格数对应心率）</p>
                    """.trimIndent()
                ),
                ManualSection(
                    id = "ecg_quickref",
                    title = "五、值班心电图快速决策参考",
                    htmlContent = """
                    <table border="1" cellpadding="8" cellspacing="0" style="border-collapse:collapse;width:100%;">
                    <tr style="background-color:#ffebee;"><th>心电图发现</th><th>紧急程度</th><th>值班处理</th></tr>
                    <tr><td><strong>ST段抬高</strong>（≥2个连续导联）</td><td style="color:#d32f2f;"><strong>紧急</strong></td><td>立即请心内科会诊，排查急性心梗</td></tr>
                    <tr><td><strong>QTc＞500ms</strong></td><td style="color:#d32f2f;"><strong>紧急</strong></td><td>停药、查电解质、心电监护</td></tr>
                    <tr><td><strong>高度房室传导阻滞</strong></td><td style="color:#d32f2f;"><strong>紧急</strong></td><td>心内科会诊，评估临时起搏</td></tr>
                    <tr><td><strong>室性心动过速</strong></td><td style="color:#d32f2f;"><strong>紧急</strong></td><td>评估血流动力学，不稳定者电复律</td></tr>
                    <tr><td><strong>心房颤动伴快速心室率</strong></td><td style="color:#f57c00;"><strong>较紧急</strong></td><td>控制心室率，排查病因</td></tr>
                    <tr><td><strong>新发左束支阻滞</strong></td><td style="color:#f57c00;"><strong>较紧急</strong></td><td>评估有无急性冠脉事件</td></tr>
                    <tr><td><strong>T波高尖</strong>（高钾血症）</td><td style="color:#d32f2f;"><strong>紧急</strong></td><td>急查血钾，立即处理</td></tr>
                    <tr><td>非特异性ST-T改变</td><td>常规</td><td>结合临床，动态观察</td></tr>
                    </table>
                    """.trimIndent()
                ),
                ManualSection(
                    id = "ecg_advice",
                    title = "六、学习建议",
                    htmlContent = """
                    <ol>
                    <li><strong>每日读图</strong>：坚持每天阅读2-3份心电图，先按标准化流程自行判读，再对照报告</li>
                    <li><strong>结合临床</strong>：将心电图与患者临床表现结合分析，而非孤立读图</li>
                    <li><strong>关注肿瘤专科特色</strong>：重点掌握抗肿瘤药物相关心电图改变及肿瘤急症的心电图表现</li>
                    <li><strong>善用图谱</strong>：先掌握典型图谱，再逐步过渡到不典型和复杂图形</li>
                    <li><strong>定期复习</strong>：建立自己的心电图图库，定期回顾已学内容</li>
                    </ol>
                    <hr/>
                    <p class="warning"><strong>提示</strong>：本手册为肿瘤科值班心电图学习参考，具体心电图判读应结合患者临床表现、既往心电图对比及心内科专业意见。危急心电图改变需立即启动相应急救流程。建议将本手册与医院心电图室资源、心内科会诊制度配合使用。</p>
                    """.trimIndent()
                )
            )
        )
    )

    /** 根据 categoryId 和 sectionId 查找章节 */
    fun findSection(categoryId: String, sectionId: String): ManualSection? {
        return categories.find { it.id == categoryId }?.sections?.find { it.id == sectionId }
    }

    /** 搜索所有章节 */
    fun search(keyword: String): List<Pair<ManualCategory, ManualSection>> {
        val results = mutableListOf<Pair<ManualCategory, ManualSection>>()
        for (cat in categories) {
            for (sec in cat.sections) {
                if (sec.title.contains(keyword) || sec.htmlContent.contains(keyword)) {
                    results.add(cat to sec)
                }
            }
        }
        return results
    }
}
