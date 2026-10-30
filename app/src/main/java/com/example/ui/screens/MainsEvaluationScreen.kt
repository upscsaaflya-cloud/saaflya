package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CurriculumSeedData
import com.example.data.model.AppLanguage
import com.example.data.model.ExamCategory
import com.example.data.model.ExamSubject
import com.example.data.model.MainsEvaluation
import com.example.viewmodel.AiTaskState
import com.example.viewmodel.CloudDataState

@Composable
fun MainsEvaluationScreen(
    language: AppLanguage,
    selectedCategory: ExamCategory,
    onSelectCategory: (ExamCategory) -> Unit,
    selectedSubject: ExamSubject,
    onSelectSubject: (ExamSubject) -> Unit,
    mainsEvaluationState: AiTaskState<MainsEvaluation>,
    mainsHistoryState: CloudDataState<List<MainsEvaluation>>,
    onEvaluateMains: (ExamCategory, ExamSubject, String, String) -> Unit,
    onDeleteEvaluation: (String) -> Unit
) {
    val defaultPrompt = remember(language) {
        val first = CurriculumSeedData.mainsPracticePrompts.first()
        if (language == AppLanguage.GUJARATI) first.promptGu else first.promptEn
    }

    var questionPrompt by rememberSaveable { mutableStateOf(defaultPrompt) }
    var studentAnswer by rememberSaveable { mutableStateOf("") }
    var activeStructureHint by remember {
        mutableStateOf(
            if (language == AppLanguage.GUJARATI) {
                CurriculumSeedData.mainsPracticePrompts.first().sampleStructureHintGu
            } else {
                CurriculumSeedData.mainsPracticePrompts.first().sampleStructureHintEn
            }
        )
    }

    val wordCount = remember(studentAnswer) {
        if (studentAnswer.isBlank()) 0 else studentAnswer.trim().split("\\s+".toRegex()).size
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header & Sample Prompt Picker
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.GUJARATI) {
                                    "મેઇન્સ વર્ણનાત્મક ઉત્તર મૂલ્યાંકન (Mains Answer Evaluation)"
                                } else {
                                    "Mains Descriptive Answer Evaluation (AI Examiner)"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (language == AppLanguage.GUJARATI) {
                                "તમારો વર્ણનાત્મક જવાબ ગુજરાતી અથવા અંગ્રેજીમાં પેસ્ટ/ટાઈપ કરો. AI તમારી પ્રસ્તાવના (Introduction), મુખ્ય ભાગ (Body), ઉપસંહાર (Conclusion) અને મહત્વના મુદ્દાઓનું વિશ્લેષણ કરશે."
                            } else {
                                "Paste or write your descriptive answer in Gujarati or English. Get structured AI feedback on Introduction, Body, Conclusion, key points covered, and model suggestions."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Exam & Subject chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ExamCategory.entries.forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { onSelectCategory(cat) },
                                    label = {
                                        Text(if (language == AppLanguage.GUJARATI) cat.titleGu else cat.titleEn)
                                    }
                                )
                            }
                            ExamSubject.entries.forEach { subj ->
                                FilterChip(
                                    selected = selectedSubject == subj,
                                    onClick = { onSelectSubject(subj) },
                                    label = {
                                        Text(if (language == AppLanguage.GUJARATI) subj.titleGu else subj.titleEn)
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (language == AppLanguage.GUJARATI) {
                                "અભ્યાસક્રમ આધારિત પ્રેક્ટિસ પ્રશ્નો (ટેપ કરીને પસંદ કરો):"
                            } else {
                                "Quick-Load Syllabus Practice Questions:"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CurriculumSeedData.mainsPracticePrompts.forEachIndexed { idx, promptItem ->
                                OutlinedButton(
                                    onClick = {
                                        questionPrompt = if (language == AppLanguage.GUJARATI) {
                                            promptItem.promptGu
                                        } else {
                                            promptItem.promptEn
                                        }
                                        activeStructureHint = if (language == AppLanguage.GUJARATI) {
                                            promptItem.sampleStructureHintGu
                                        } else {
                                            promptItem.sampleStructureHintEn
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("mains_sample_prompt_$idx")
                                ) {
                                    Text(
                                        text = "${promptItem.examCategory} • ${promptItem.subject} (${promptItem.marks}M / ${promptItem.wordLimit}W)",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Question & Descriptive Answer Input Form
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (language == AppLanguage.GUJARATI) "મેઇન્સ પ્રશ્ન (Mains Question Prompt)" else "Mains Question Prompt",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = questionPrompt,
                            onValueChange = { questionPrompt = it },
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mains_question_input")
                        )

                        if (activeStructureHint.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = activeStructureHint,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (language == AppLanguage.GUJARATI) "તમારો વર્ણનાત્મક ઉત્તર (Your Answer)" else "Your Descriptive Answer",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (language == AppLanguage.GUJARATI) "$wordCount શબ્દો (Words)" else "$wordCount Words",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = studentAnswer,
                            onValueChange = { studentAnswer = it },
                            placeholder = {
                                Text(
                                    text = if (language == AppLanguage.GUJARATI) {
                                        "અહીં તમારો જવાબ પેસ્ટ કરો અથવા લખો (પ્રસ્તાવના, મુખ્ય મુદ્દાઓ અને ઉપસંહાર સાથે)..."
                                    } else {
                                        "Paste or write your structured Mains answer here (Introduction, Body points, and Conclusion)..."
                                    }
                                )
                            },
                            minLines = 6,
                            maxLines = 14,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mains_answer_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    studentAnswer = if (language == AppLanguage.GUJARATI) {
                                        """
                                        પ્રસ્તાવના: સોલંકી કાળ (૧૧મીથી ૧૩મી સદી) દરમિયાન ગુજરાતમાં વિકસેલી 'મારુ-ગુર્જર' (સોલંકી) સ્થાપત્ય શૈલી ભારતીય નાગર મંદિર સ્થાપત્યનું ઉત્કૃષ્ટ સ્વરૂપ છે.
                                        
                                        મુખ્ય ભાગ:
                                        ૧. મંદિર રચના: ગર્ભગૃહ, ગૂઢમંડપ, સભામંડપ (રંગમંડપ) અને આગળના ભાગમાં કુંડ તથા કીર્તિતોરણ તેની મુખ્ય લાક્ષણિકતા છે.
                                        ૨. મોઢેરાનું સૂર્યમંદિર: ભીમદેવ પ્રથમ દ્વારા નિર્મિત આ મંદિરમાં સૂર્યકુંડ (રામકુંડ) અને ૫૨ સ્તંભોવાળો સભામંડપ આવેલો છે.
                                        ૩. રાણીની વાવ (પાટણ): રાણી ઉદયમતી દ્વારા બંધાયેલી સાત માળની જયા પ્રકારની વાવ જળ-પવિત્રતા અને શિલ્પકલાનો બેનમૂન નમૂનો છે.
                                        ૪. વડનગરનું કીર્તિતોરણ અને દેલવાડાના જૈન દેરાસરોમાં સૂક્ષ્મ આરસની કોતરણી જોવા મળે છે.
                                        
                                        ઉપસંહાર: મારુ-ગુર્જર શૈલીએ ગુજરાતની સ્થાપત્ય ઓળખને વૈશ્વિક સ્તરે યુનેસ્કો વર્લ્ડ હેરિટેજમાં સ્થાન અપાવ્યું છે.
                                        """.trimIndent()
                                    } else {
                                        """
                                        Introduction: Developed between the 11th and 13th centuries under the Solanki (Chaulukya) rulers of Anhilwad Patan, the Maru-Gurjara architectural style represents a regional pinnacle of the Nagara temple tradition in western India.
                                        
                                        Body:
                                        1. Structural Layout: Characterized by Garbhagriha, Gudhamandapa, an open pillared Sabha Mandapa, ornamental Kirti Toranas (arches), and stepped water tanks (Kunda).
                                        2. Sun Temple, Modhera (1026 CE): Commissioned by Bhimdev I, featuring the geometric Surya Kund, 52 intricately carved pillars representing weeks of the year, and equinox alignment.
                                        3. Rani ki Vav, Patan: Built by Queen Udayamati as an inverted temple sanctifying water across seven subterranean storeys with Vishnu dashavatara sculptures.
                                        4. Jain Temple Architecture: Vimal Vasahi and Luna Vasahi at Mount Abu, and Ajitnath Temple at Taranga Hill showcase marble filigree mastery.
                                        
                                        Conclusion: Maru-Gurjara architecture seamlessly fused spiritual symbolism with hydro-engineering, earning global recognition on the UNESCO World Heritage list.
                                        """.trimIndent()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("fill_sample_answer_button")
                            ) {
                                Text(
                                    text = if (language == AppLanguage.GUJARATI) "નમૂનાનો ઉત્તર ભરો" else "Insert Sample Answer"
                                )
                            }

                            Button(
                                onClick = {
                                    onEvaluateMains(
                                        selectedCategory,
                                        selectedSubject,
                                        questionPrompt,
                                        studentAnswer
                                    )
                                },
                                enabled = mainsEvaluationState !is AiTaskState.Loading,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("evaluate_mains_button")
                            ) {
                                if (mainsEvaluationState is AiTaskState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (language == AppLanguage.GUJARATI) "મૂલ્યાંકન ચાલુ છે..." else "Evaluating...")
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (language == AppLanguage.GUJARATI) "AI મૂલ્યાંકન કરો" else "Evaluate with AI"
                                    )
                                }
                            }
                        }

                        if (mainsEvaluationState is AiTaskState.Error) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = mainsEvaluationState.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // 3. Active AI Evaluation Breakdown
            if (mainsEvaluationState is AiTaskState.Success) {
                item {
                    MainsEvaluationResultCard(
                        evaluation = mainsEvaluationState.result,
                        language = language,
                        isLatestResult = true,
                        onDelete = null
                    )
                }
            }

            // 4. Past Evaluations History from Firestore
            if (mainsHistoryState is CloudDataState.Success && mainsHistoryState.data.isNotEmpty()) {
                item {
                    Text(
                        text = if (language == AppLanguage.GUJARATI) {
                            "અગાઉના મૂલ્યાંકનનો ઇતિહાસ (Saved Mains Evaluations)"
                        } else {
                            "Saved Mains Evaluation History"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(
                    items = mainsHistoryState.data,
                    key = { it.id.ifBlank { it.questionPrompt.hashCode().toString() } }
                ) { evalItem ->
                    MainsEvaluationResultCard(
                        evaluation = evalItem,
                        language = language,
                        isLatestResult = false,
                        onDelete = if (evalItem.id.isNotBlank()) {
                            { onDeleteEvaluation(evalItem.id) }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
fun MainsEvaluationResultCard(
    evaluation: MainsEvaluation,
    language: AppLanguage,
    isLatestResult: Boolean,
    onDelete: (() -> Unit)?
) {
    ElevatedCard(
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "${evaluation.overallScore} / 10",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "${evaluation.examCategory} • ${evaluation.subject}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (isLatestResult) {
                            Text(
                                text = if (language == AppLanguage.GUJARATI) "AI માળખાગત મૂલ્યાંકન રિપોર્ટ" else "AI Structured Evaluation Breakdown",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Evaluation",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = evaluation.questionPrompt,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Structure Breakdown: Introduction, Body, Conclusion
            StructureSectionBox(
                title = if (language == AppLanguage.GUJARATI) "૧. પ્રસ્તાવના (Introduction)" else "1. Introduction Evaluation",
                content = evaluation.introFeedback
            )
            Spacer(modifier = Modifier.height(10.dp))
            StructureSectionBox(
                title = if (language == AppLanguage.GUJARATI) "૨. મુખ્ય ભાગ (Body & Analytical Depth)" else "2. Body & Content Structure",
                content = evaluation.bodyFeedback
            )
            Spacer(modifier = Modifier.height(10.dp))
            StructureSectionBox(
                title = if (language == AppLanguage.GUJARATI) "૩. ઉપસંહાર (Conclusion & Way Forward)" else "3. Conclusion Evaluation",
                content = evaluation.conclusionFeedback
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Key Points Covered
            if (evaluation.keyPointsCovered.isNotEmpty()) {
                Text(
                    text = if (language == AppLanguage.GUJARATI) "✓ આવરી લેવાયેલા મુખ્ય મુદ્દાઓ (Key Points Covered)" else "✓ Key Points Covered",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                evaluation.keyPointsCovered.forEach { point ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = point,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Suggestions for Improvement
            if (evaluation.improvementSuggestions.isNotEmpty()) {
                Text(
                    text = if (language == AppLanguage.GUJARATI) "⚡ સુધારા માટેના સૂચનો (Suggestions for Improvement)" else "⚡ Suggestions for Improvement",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                evaluation.improvementSuggestions.forEach { suggestion ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.TipsAndUpdates,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Model Answer Blueprint
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (language == AppLanguage.GUJARATI) "આદર્શ ઉત્તરની રૂપરેખા (Model Answer Blueprint)" else "Model Answer Blueprint",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = evaluation.modelAnswerSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun StructureSectionBox(
    title: String,
    content: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
