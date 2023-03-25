/*
 * Copyright (C) 2023 Fabian Andera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.conena.anrdetective.kotlin.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

class MainActivity : AppCompatActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(text = stringResource(id = R.string.app_name))
                            }
                        )
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues = paddingValues)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(all = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            var blockFor: Long by rememberSaveable {
                                mutableStateOf(3L)
                            }
                            Text(
                                text = stringResource(id = R.string.label_intro, blockFor),
                                textAlign = TextAlign.Center
                            )
                            Slider(
                                value = blockFor.toFloat(),
                                valueRange = 0f..10f,
                                steps = 11,
                                onValueChange = { newValue ->
                                    blockFor = newValue.toLong()
                                }
                            )
                            ActionButton(actionType = ActionType.SLEEP, blockFor = blockFor * 1_000L)
                            ActionButton(actionType = ActionType.LOOP, blockFor = blockFor * 1_000L)
                        }
                    }
                }
            }
        }
    }

}

@Composable
private fun ActionButton(actionType: ActionType, blockFor: Long) {
    Button(modifier = Modifier.padding(top = 8.dp), onClick = {
        actionType.action(blockFor)
    }) {
        Text(text = stringResource(id = actionType.label))
    }
}

private enum class ActionType(@StringRes val label: Int, val action: (blockFor: Long) -> Unit) {
    SLEEP(
        label = R.string.label_sleep,
        action = { blockFor ->
            Thread.sleep(blockFor)
        }
    ),
    LOOP(
        label = R.string.label_loop,
        action = { blockFor ->
            val waitUntil = System.currentTimeMillis() + blockFor
            while (System.currentTimeMillis() < waitUntil) {}
        }
    )
}