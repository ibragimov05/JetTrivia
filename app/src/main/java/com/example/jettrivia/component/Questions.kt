package com.example.jettrivia.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jettrivia.model.QuestionItem
import com.example.jettrivia.screens.QuestionsViewModel
import com.example.jettrivia.util.AppColors

@Composable
fun Questions(viewModel: QuestionsViewModel) {
    val questions = viewModel.data.value.data?.toMutableList()

    val currentQuestionIndex = remember { mutableIntStateOf(0) }

    if (viewModel.data.value.loading == true || questions == null) {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center,
            content = {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                    Text(text = "Loading...")
                }
            },
        )
    } else {
        if (questions.size >= currentQuestionIndex.intValue) {
            QuestionDisplay(question = questions[currentQuestionIndex.intValue],
                questionIndex = currentQuestionIndex,
                viewModel = viewModel,
                onNextClicked = { currentQuestionIndex.intValue = it })
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
                content = {
                    Text(text = "no questions left")
                },
            )
        }
    }
}

@Composable
fun QuestionDisplay(
    question: QuestionItem,
    questionIndex: MutableIntState,
    viewModel: QuestionsViewModel,
    onNextClicked: (Int) -> Unit = {}
) {
    val choicesState = remember(question) { question.choices.toMutableList() }

    val answerState = remember(question) { mutableStateOf<Int?>(null) }

    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

    val correctAnswerState = remember(question) {
        mutableStateOf<Boolean?>(null)
    }

    val updateAnswer: (Int) -> Unit = remember(question) {
        {
            answerState.value = it
            correctAnswerState.value = choicesState[it].lowercase() == question.answer.lowercase()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors.mDarkPurple,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            ShowProgress(score = questionIndex.intValue)


            QuestionTracker(
                counter = questionIndex.intValue + 1, outOf = viewModel.getQuestionsSize()
            )

            DrawDottedLine(pathEffect = pathEffect)

            Column {
                Text(
                    text = question.question,
                    modifier = Modifier
                        .padding(6.dp)
                        .align(alignment = Alignment.Start)
                        .fillMaxHeight(fraction = 0.3f),
                    fontSize = 17.sp,
                    color = AppColors.mOffWhite,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                )

                // Choices
                choicesState.forEachIndexed { index, answerText ->
                    Row(
                        modifier = Modifier
                            .padding(3.dp)
                            .fillMaxWidth()
                            .height(45.dp)
                            .border(
                                width = 4.dp, brush = Brush.linearGradient(
                                    colors = listOf(
                                        AppColors.mOffDarkPurple,
                                        AppColors.mOffDarkPurple,
                                    ),
                                ), shape = RoundedCornerShape(15.dp)
                            )
                            .clip(RoundedCornerShape(50.dp))
                            .background(AppColors.mTransparent),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = (answerState.value == index),
                            onClick = { updateAnswer(index) },
                            modifier = Modifier.padding(start = 16.dp),
                            colors = RadioButtonDefaults.colors(
                                selectedColor = if (correctAnswerState.value == true && index == answerState.value) {
                                    Color.Green
                                } else {
                                    Color.Red
                                }
                            )
                        )
                        val annotatedString = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = if (correctAnswerState.value == true && index == answerState.value) {
                                        Color.Green
                                    } else if (correctAnswerState.value == false && index == answerState.value) {
                                        Color.Red
                                    } else {
                                        AppColors.mOffWhite
                                    },
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Light,
                                )
                            ) {
                                append(answerText)
                            }
                        }

                        Text(text = annotatedString, modifier = Modifier.padding(6.dp))
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Button(
                        onClick = {
                            if (correctAnswerState.value != null) {
                                onNextClicked(questionIndex.intValue + 1)
                            }
                        },
                        modifier = Modifier.padding(3.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.mLightBlue,
                        ),
                    ) {
                        Text(
                            text = "Next",
                            modifier = Modifier.padding(8.dp),
                            color = AppColors.mOffWhite,
                            fontSize = 17.sp,
                        )
                    }
                }

            }
        }

    }
}

@Composable
fun ShowProgress(score: Int = 12) {
    val gradient = Brush.linearGradient(
        listOf(Color(0xFFF95075), Color(0xFFBE6BE5))
    )

    val progressFactory = remember(score) {
        mutableFloatStateOf(score * 0.005f)
    }

    Row(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
            .height(45.dp)
            .border(
                width = 4.dp, brush = Brush.linearGradient(
                    colors = listOf(AppColors.mLightPurple, AppColors.mLightPurple)
                ), shape = RoundedCornerShape(34.dp)
            )
            .clip(RoundedCornerShape(50.dp))
            .background(AppColors.mTransparent),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            contentPadding = PaddingValues(3.dp),
            onClick = {/*TODO*/ },
            modifier = Modifier
                .fillMaxWidth(progressFactory.floatValue)
                .background(brush = gradient),
            enabled = false,
            elevation = null,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
            ),
        ) {
            Text(
                text = (score * 10).toString(),
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(23.dp))
                    .fillMaxHeight(0.85f)
                    .fillMaxWidth()
                    .padding(6.dp),
                color = AppColors.mOffWhite,
                textAlign = TextAlign.Center,
            )
        }

    }
}

@Composable
fun DrawDottedLine(pathEffect: PathEffect) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp),
    ) {
        drawLine(
            color = AppColors.mLightGray,
            start = Offset(0f, 0f),
            end = Offset(size.width, y = 0f),
            pathEffect = pathEffect
        )
    }
}

@Composable
fun QuestionTracker(counter: Int, outOf: Int) {
    Text(
        text = buildAnnotatedString {
            withStyle(style = ParagraphStyle(textIndent = TextIndent.None)) {
                withStyle(
                    style = SpanStyle(
                        color = AppColors.mLightGray, fontWeight = FontWeight.Bold, fontSize = 27.sp
                    )
                ) {
                    append("Question: $counter/")
                    withStyle(
                        style = SpanStyle(
                            color = AppColors.mLightGray,
                            fontWeight = FontWeight.Light,
                            fontSize = 14.sp,
                        ),
                    ) { append("$outOf") }
                }
            }
        },
        modifier = Modifier.padding(20.dp),
    )
}
