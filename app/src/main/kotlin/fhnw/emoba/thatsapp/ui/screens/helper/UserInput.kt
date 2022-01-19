package fhnw.emoba.thatsapp.ui.screens.helper

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fhnw.emoba.thatsapp.model.ChatPayloadContents
import fhnw.emoba.thatsapp.model.ThatsAppModel

/**
 * Inspired by https://github.com/elye/demo_android_jetchat_with_websocket/blob/main/app/src/main/java/com/example/compose/jetchat/conversation/UserInput.kt
 */
@ExperimentalComposeUiApi
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserInput(
    model: ThatsAppModel,
    modifier: Modifier = Modifier,
) {
    var currentMessageOption by rememberSaveable { mutableStateOf(ChatPayloadContents.NONE) }

    // Used to decide if the keyboard should be shown
    var textFieldFocusState by remember { mutableStateOf(false) }

        Column(modifier = modifier) {

            MessageOptions(
                onOptionChange = { currentMessageOption = it },
                currentMessageOption = currentMessageOption,
                model = model
            )

            OptionExpanded(
                onTextAdded = { model.textMessageToSend = model.textMessageToSend + it  },
                currentMessageOption = currentMessageOption,
                model = model
            )

            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                    UserInputText(
                        model = model,
                        messageValue = model.textMessageToSend,
                        onTextChanged = { model.textMessageToSend = it },
                        // Only show the keyboard if there's no input option and text field has focus
                        keyboardShown = currentMessageOption == ChatPayloadContents.TEXT && textFieldFocusState,
                        // Close extended option if text field receives focus
                        onTextFieldFocused =  { focused ->
                            if (focused) {
                                currentMessageOption = ChatPayloadContents.TEXT
                            }
                            textFieldFocusState = focused
                        },
                    )
                    SendButton(
                        sendMessageEnabled =
                            if (
                                currentMessageOption == ChatPayloadContents.NONE ||
                                currentMessageOption == ChatPayloadContents.TEXT ||
                                currentMessageOption == ChatPayloadContents.EMOJI
                            ) {
                                model.textMessageToSend.isNotEmpty()
                            } else if (currentMessageOption == ChatPayloadContents.IMAGE ||
                                currentMessageOption == ChatPayloadContents.PHOTO) {
                                model.imageMessageToSend != null
                            } else if (currentMessageOption == ChatPayloadContents.LOCATION) {
                                model.locationMessageToSend != null
                            } else {
                                false
                            },
                        onMessageSend = {
                            model.currentMessageOptionSend = currentMessageOption
                            model.prePublish()
                        }
                    )
            }
        }
}

@Composable
private fun MessageOptions(
    onOptionChange: (ChatPayloadContents) -> Unit,
    currentMessageOption: ChatPayloadContents,
    model: ThatsAppModel
) {
    // Choosing image from gallery
    val context = LocalContext.current
    var uri : Uri?
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
            uri = it
            uri.let {
                if (uri != null) {
                    if (Build.VERSION.SDK_INT < 28) {
                        model.imageMessageToSend = MediaStore.Images.Media.getBitmap(
                            context.contentResolver,
                            uri
                        )
                    } else {
                        val source = ImageDecoder.createSource(context.contentResolver, uri!!)
                        model.imageMessageToSend = ImageDecoder.decodeBitmap(source)
                    }
                } else { // uri is null
                    onOptionChange(ChatPayloadContents.NONE)
                }
            }
        }

    Row(
        modifier = Modifier
            .height(45.dp)
            .padding(top = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        InputOptionButton(
            onClick = { onOptionChange(ChatPayloadContents.EMOJI) },
            icon = Icons.Outlined.Mood,
            selected = currentMessageOption == ChatPayloadContents.EMOJI,
            description = "Send emoji"
        )
        InputOptionButton(
            onClick = {
                onOptionChange(ChatPayloadContents.PHOTO)
                model.takePhotoForMessage()
             },
            icon = Icons.Outlined.PhotoCamera,
            selected = currentMessageOption == ChatPayloadContents.PHOTO,
            description = "Send photo"
        )
        InputOptionButton(
            onClick = {
                onOptionChange(ChatPayloadContents.IMAGE)
                launcher.launch("image/*")
             },
            icon = Icons.Outlined.InsertPhoto,
            selected = currentMessageOption == ChatPayloadContents.IMAGE,
            description = "Send image from library"
        )
        InputOptionButton(
            onClick = {
                onOptionChange(ChatPayloadContents.LOCATION)
                model.saveCurrentPosition()
              },
            icon = Icons.Outlined.Place,
            selected = currentMessageOption == ChatPayloadContents.LOCATION,
            description = "Send location"
        )
    }
}


@Composable
private fun InputOptionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    description: String,
    selected: Boolean
) {
    val backgroundModifier = if (selected) {
        Modifier.background(
            color = MaterialTheme.colors.secondary.copy(alpha = 0.15f),
            shape = RoundedCornerShape(25.dp)
        )
    } else {
        Modifier
    }

    IconButton(
        onClick = onClick,
        modifier = backgroundModifier
    ) {
        val tint = if (selected) {
            MaterialTheme.colors.secondary
        } else {
            MaterialTheme.colors.secondaryVariant
        }
        Icon(
            icon,
            tint = tint,
            modifier = Modifier.padding(0.dp),
            contentDescription = description
        )
    }
}


@Composable
private fun OptionExpanded(
    currentMessageOption: ChatPayloadContents,
    onTextAdded: (String) -> Unit,
    model: ThatsAppModel
) {
    if (currentMessageOption == ChatPayloadContents.TEXT || currentMessageOption == ChatPayloadContents.NONE) return

    // Request focus to force the TextField to lose it
    val focusRequester = FocusRequester()
    // If the option is shown, always request focus to trigger a TextField.onFocusChange.
    SideEffect {
        if (currentMessageOption == ChatPayloadContents.EMOJI ||
            currentMessageOption == ChatPayloadContents.IMAGE ||
            currentMessageOption == ChatPayloadContents.PHOTO ||
            currentMessageOption == ChatPayloadContents.LOCATION ) {
            focusRequester.requestFocus()
        }
    }
    Surface {
        when (currentMessageOption) {
            ChatPayloadContents.EMOJI -> { EmojiOption(model, onTextAdded, focusRequester) }
            ChatPayloadContents.PHOTO -> { ImageOption(model, focusRequester) }
            ChatPayloadContents.IMAGE -> { ImageOption(model, focusRequester) }
            ChatPayloadContents.LOCATION -> { LocationOption(model, focusRequester) }
            else -> {  }
        }
    }
}


/* OPTIONS */
@Composable
fun EmojiOption(
    model: ThatsAppModel,
    onTextAdded: (String) -> Unit,
    focusRequester: FocusRequester
) {
    Option(model = model,
        modifierCol = Modifier
            .focusRequester(focusRequester) // Requests focus when the Emoji option is displayed
            // Make the emoji option focusable so it can steal focus from TextField
            .focusTarget(),

        // Scrolling not correctly implemented (see EmojiTable)
        modifierRow = Modifier.verticalScroll(rememberScrollState()),
        nullable = false,
        rowContent = {
            EmojiTable(
                onTextAdded,
                modifier = Modifier.padding(8.dp)
            )
        }
    )
}

@Composable
fun ImageOption(
    model: ThatsAppModel,
    focusRequester: FocusRequester
) {
    Option(model = model,
        modifierCol = Modifier
            .focusRequester(focusRequester)
            .focusTarget()
            .fillMaxWidth()
            .height(190.dp),
        modifierRow = Modifier.fillMaxWidth(),
        nullable = model.imageMessageToSend,
        rowContent = {
            Image(bitmap = model.imageMessageToSend!!.asImageBitmap(),
                contentDescription = "Image to send",
                modifier = Modifier.size(175.dp)
            )
        }
    )
}

@Composable
fun LocationOption(
    model: ThatsAppModel,
    focusRequester: FocusRequester
) {

    Option(
        model = model,
        modifierCol = Modifier
            .focusRequester(focusRequester)
            .focusTarget()
            .fillMaxWidth()
            .height(50.dp),
        modifierRow = Modifier.fillMaxWidth(),
        nullable = model.locationMessageToSend,
        rowContent = {
            Icon(
                imageVector = Icons.Filled.Map,
                contentDescription = "Location",
                tint = MaterialTheme.colors.secondary
            )
            Text(model.locationMessageToSend!!.dms())
        }
    )
}

val KeyboardShownKey = SemanticsPropertyKey<Boolean>("KeyboardShownKey")
var SemanticsPropertyReceiver.keyboardShownProperty by KeyboardShownKey

@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@Composable
private fun UserInputText(
    model: ThatsAppModel,
    onTextChanged: (String) -> Unit,
    messageValue: String,
    onTextFieldFocused: (Boolean) -> Unit,
    keyboardShown: Boolean
) {
    val keyboard = LocalSoftwareKeyboardController.current

    Column(Modifier.semantics {
        keyboardShownProperty = keyboardShown
    }) {
        var lastFocusState by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = messageValue,
            onValueChange = {
                onTextChanged(it)
                model.isTypingSend = true
            },
            singleLine = false,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboard?.hide() },
                onSend = { keyboard?.hide() }
            ),
            colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = MaterialTheme.colors.secondary),
            placeholder = { Text("Message") },
            modifier = Modifier
                .width(320.dp)
                .onFocusChanged { state ->
                    if (lastFocusState != state.isFocused) {
                        onTextFieldFocused(state.isFocused)
                    }
                    lastFocusState = state.isFocused
                }
        )
    }
}

@Composable
private fun SendButton(
    sendMessageEnabled: Boolean,
    onMessageSend: () -> Unit
){
    // Send button
    Column {
        IconButton(
            modifier = Modifier
                .height(47.dp)
                .width(47.dp)
                .padding(0.dp)
                .background(
                    color = MaterialTheme.colors.secondary.copy(alpha = 0.15f),
                    shape = CircleShape
                ),
            enabled = sendMessageEnabled,
            onClick = onMessageSend
        ) {
            val tint = if (sendMessageEnabled) {
                MaterialTheme.colors.secondary
            } else {
                MaterialTheme.colors.secondaryVariant
            }
            Icon(
                imageVector = Icons.Outlined.Send,
                tint = tint,
                contentDescription = "Send",
                modifier = Modifier.padding(0.dp)
            )
        }
    }
}


/* FURTHER */

@Composable
fun Option(
    model: ThatsAppModel, modifierCol: Modifier, modifierRow: Modifier,
    nullable: Any?, rowContent: @Composable () -> Unit){
    Column(modifier = modifierCol,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = modifierRow,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            if(model.isLoading){
                LoadingIndicator()
            } else {
                if(nullable != null) {
                    rowContent()
                }
            }
        }
    }
}

/* Not fully working: only the first 40 emojis are displayed:
   can't scroll through whole list. LazyVerticalGrid couldn't be implemented. */
@Composable
private fun EmojiTable(
    onTextAdded: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        repeat(4) { x ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(EMOJI_COLUMNS) { y ->
                    val emoji = emojis[x * EMOJI_COLUMNS + y]
                    Text(
                        modifier = Modifier
                            .clickable(onClick = { onTextAdded(emoji) })
                            .sizeIn(minWidth = 35.dp, minHeight = 40.dp)
                            .padding(0.dp),
                        text = emoji,
                        style = LocalTextStyle.current.copy(
                            fontSize = 23.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}

private const val EMOJI_COLUMNS = 10

private val emojis = listOf(
    "\ud83d\ude00", // Grinning Face
    "\ud83d\ude01", // Grinning Face With Smiling Eyes
    "\ud83d\ude02", // Face With Tears of Joy
    "\ud83d\ude03", // Smiling Face With Open Mouth
    "\ud83d\ude04", // Smiling Face With Open Mouth and Smiling Eyes
    "\ud83d\ude05", // Smiling Face With Open Mouth and Cold Sweat
    "\ud83d\ude06", // Smiling Face With Open Mouth and Tightly-Closed Eyes
    "\ud83d\ude09", // Winking Face
    "\ud83d\ude0a", // Smiling Face With Smiling Eyes
    "\ud83d\ude0b", // Face Savouring Delicious Food
    "\ud83d\ude0e", // Smiling Face With Sunglasses
    "\ud83d\ude0d", // Smiling Face With Heart-Shaped Eyes
    "\ud83d\ude18", // Face Throwing a Kiss
    "\ud83d\ude17", // Kissing Face
    "\ud83d\ude19", // Kissing Face With Smiling Eyes
    "\ud83d\ude1a", // Kissing Face With Closed Eyes
    "\u263a", // White Smiling Face
    "\ud83d\ude42", // Slightly Smiling Face
    "\ud83e\udd17", // Hugging Face
    "\ud83d\ude07", // Smiling Face With Halo
    "\ud83e\udd13", // Nerd Face
    "\ud83e\udd14", // Thinking Face
    "\ud83d\ude10", // Neutral Face
    "\ud83d\ude11", // Expressionless Face
    "\ud83d\ude36", // Face Without Mouth
    "\ud83d\ude44", // Face With Rolling Eyes
    "\ud83d\ude0f", // Smirking Face
    "\ud83d\ude23", // Persevering Face
    "\ud83d\ude25", // Disappointed but Relieved Face
    "\ud83d\ude2e", // Face With Open Mouth
    "\ud83e\udd10", // Zipper-Mouth Face
    "\ud83d\ude2f", // Hushed Face
    "\ud83d\ude2a", // Sleepy Face
    "\ud83d\ude2b", // Tired Face
    "\ud83d\ude34", // Sleeping Face
    "\ud83d\ude0c", // Relieved Face
    "\ud83d\ude1b", // Face With Stuck-Out Tongue
    "\ud83d\ude1c", // Face With Stuck-Out Tongue and Winking Eye
    "\ud83d\ude1d", // Face With Stuck-Out Tongue and Tightly-Closed Eyes
    "\ud83d\ude12", // Unamused Face
    "\ud83d\ude13", // Face With Cold Sweat
    "\ud83d\ude14", // Pensive Face
    "\ud83d\ude15", // Confused Face
    "\ud83d\ude43", // Upside-Down Face
    "\ud83e\udd11", // Money-Mouth Face
    "\ud83d\ude32", // Astonished Face
    "\ud83d\ude37", // Face With Medical Mask
    "\ud83e\udd12", // Face With Thermometer
    "\ud83e\udd15", // Face With Head-Bandage
    "\u2639", // White Frowning Face
    "\ud83d\ude41", // Slightly Frowning Face
    "\ud83d\ude16", // Confounded Face
    "\ud83d\ude1e", // Disappointed Face
    "\ud83d\ude1f", // Worried Face
    "\ud83d\ude24", // Face With Look of Triumph
    "\ud83d\ude22", // Crying Face
    "\ud83d\ude2d", // Loudly Crying Face
    "\ud83d\ude26", // Frowning Face With Open Mouth
    "\ud83d\ude27", // Anguished Face
    "\ud83d\ude28", // Fearful Face
    "\ud83d\ude29", // Weary Face
    "\ud83d\ude2c", // Grimacing Face
    "\ud83d\ude30", // Face With Open Mouth and Cold Sweat
    "\ud83d\ude31", // Face Screaming in Fear
    "\ud83d\ude33", // Flushed Face
    "\ud83d\ude35", // Dizzy Face
    "\ud83d\ude21", // Pouting Face
    "\ud83d\ude20", // Angry Face
    "\ud83d\ude08", // Smiling Face With Horns
    "\ud83d\udc7f", // Imp
    "\ud83d\udc79", // Japanese Ogre
    "\ud83d\udc7a", // Japanese Goblin
    "\ud83d\udc80", // Skull
    "\ud83d\udc7b", // Ghost
    "\ud83d\udc7d", // Extraterrestrial Alien
    "\ud83e\udd16", // Robot Face
    "\ud83d\udca9", // Pile of Poo
    "\ud83d\ude3a", // Smiling Cat Face With Open Mouth
    "\ud83d\ude38", // Grinning Cat Face With Smiling Eyes
    "\ud83d\ude39", // Cat Face With Tears of Joy
    "\ud83d\ude3b", // Smiling Cat Face With Heart-Shaped Eyes
    "\ud83d\ude3c", // Cat Face With Wry Smile
    "\ud83d\ude3d", // Kissing Cat Face With Closed Eyes
    "\ud83d\ude40", // Weary Cat Face
    "\ud83d\ude3f", // Crying Cat Face
    "\ud83d\ude3e", // Pouting Cat Face
    "\ud83d\udc66", // Boy
    "\ud83d\udc67", // Girl
    "\ud83d\udc68", // Man
    "\ud83d\udc69", // Woman
    "\ud83d\udc74", // Older Man
    "\ud83d\udc75", // Older Woman
    "\ud83d\udc76", // Baby
    "\ud83d\udc71", // Person With Blond Hair
    "\ud83d\udc6e", // Police Officer
    "\ud83d\udc72", // Man With Gua Pi Mao
    "\ud83d\udc73", // Man With Turban
    "\ud83d\udc77", // Construction Worker
    "\u26d1", // Helmet With White Cross
    "\ud83d\udc78", // Princess
    "\ud83d\udc82", // Guardsman
    "\ud83d\udd75", // Sleuth or Spy
    "\ud83c\udf85", // Father Christmas
    "\ud83d\udc70", // Bride With Veil
    "\ud83d\udc7c", // Baby Angel
    "\ud83d\udc86", // Face Massage
    "\ud83d\udc87", // Haircut
    "\ud83d\ude4d", // Person Frowning
    "\ud83d\ude4e", // Person With Pouting Face
    "\ud83d\ude45", // Face With No Good Gesture
    "\ud83d\ude46", // Face With OK Gesture
    "\ud83d\udc81", // Information Desk Person
    "\ud83d\ude4b", // Happy Person Raising One Hand
    "\ud83d\ude47", // Person Bowing Deeply
    "\ud83d\ude4c", // Person Raising Both Hands in Celebration
    "\ud83d\ude4f", // Person With Folded Hands
    "\ud83d\udde3", // Speaking Head in Silhouette
    "\ud83d\udc64", // Bust in Silhouette
    "\ud83d\udc65", // Busts in Silhouette
    "\ud83d\udeb6", // Pedestrian
    "\ud83c\udfc3", // Runner
    "\ud83d\udc6f", // Woman With Bunny Ears
    "\ud83d\udc83", // Dancer
    "\ud83d\udd74", // Man in Business Suit Levitating
    "\ud83d\udc6b", // Man and Woman Holding Hands
    "\ud83d\udc6c", // Two Men Holding Hands
    "\ud83d\udc6d", // Two Women Holding Hands
    "\ud83d\udc8f" // Kiss
)