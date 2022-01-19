package fhnw.emoba.thatsapp.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fhnw.emoba.thatsapp.model.Screen
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.thatsapp.ui.screens.helper.GeneralTopBar
import fhnw.emoba.thatsapp.ui.screens.helper.Notification
import fhnw.emoba.thatsapp.ui.screens.helper.NotificationHost
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun ProfileScreen(model: ThatsAppModel) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(scaffoldState = scaffoldState,
        topBar = { GeneralTopBar(model, Screen.PROFILE.title, Screen.MAIN) },
        snackbarHost = { NotificationHost(it) },
        content = { ModalBottomSheet(model) }
    )
    Notification(model, scaffoldState)
}


// TODO: ModalBottomDrawer should not be shown anymore, when action took place.
@ExperimentalComposeUiApi
@Composable
@ExperimentalMaterialApi
fun ModalBottomSheet(model: ThatsAppModel) {
    val state = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    ModalBottomSheetLayout(
        sheetState = state,
        sheetContent = {
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally) {
                if(model.textFieldProfileURL){
                    InfoTextField(
                        value = model.profileImageURL,
                        onValueChange = { model.profileImageURL = it },
                        about = "url",
                        icon = Icons.Filled.Link,
                        keyboard = keyboard,
                        focusManager = focusManager,
                        model = model,
                        modifier = Modifier.padding(5.dp, 10.dp, 5.dp, 10.dp).width(380.dp)
                    )
                }
                else {
                    ListItem(
                        text = { Text("Get image from link") },
                        icon = {
                            Icon(
                                Icons.Default.Link,
                                contentDescription = "Link"
                            )
                        },
                        modifier = Modifier.clickable { model.textFieldProfileURL = true }
                    )
                    ListItem(
                        text = { Text("Take photo from camera") },
                        icon = {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = "Camera"
                            )
                        },
                        modifier = Modifier.clickable {
                            model.takePhotoForProfileImage()

                        }
                    )
                    ListItem(
                        text = { Text("Remove image") },
                        icon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete"
                            )
                        },
                        modifier = Modifier.clickable { model.setDefaultProfileImage() }
                    )

               }
            }
        }
    ){ Body(model = model, state) }
}

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
private fun Body(model: ThatsAppModel, state: ModalBottomSheetState) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(10.dp, 30.dp, 10.dp, 10.dp)
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        ProfilePicture(model, state)
        ProfileInformation(model, modifier = Modifier.padding(0.dp, 20.dp, 0.dp, 0.dp))
    }
}

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
private fun ProfilePicture(model: ThatsAppModel, state: ModalBottomSheetState) {
    val scope = rememberCoroutineScope()

    with(model) {
        val imageModifier = Modifier
            .size(275.dp)
            .clip(CircleShape)
            Box {
                Surface(
                    //modifier = Modifier.defaultMinSize(size.dp),
                    shape = CircleShape,
                    border = BorderStroke(1.5.dp, MaterialTheme.colors.primary)
                ) {
                    if (profileImage != null) {
                        Image(
                            bitmap = profileImage!!.asImageBitmap(),
                            contentDescription = "Profile image",
                            modifier = imageModifier.clickable {
                                currentScreen = Screen.FULL_IMAGE
                                backScreen = Screen.PROFILE
                                showImageBig = profileImage!!
                            }
                        )
                    } else {
                        Icon(
                            Icons.Filled.AccountCircle,
                            contentDescription = "Empty profile image",
                            modifier = imageModifier,
                            tint = MaterialTheme.colors.secondary.copy(alpha = 0.3f)
                        )
                    }
                }
                IconButton(
                    onClick = { scope.launch { state.show() } }, // Show ModalBottomSheet
                    content = { Icon(imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = "Change profile image",
                        tint = MaterialTheme.colors.onSecondary
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .height(60.dp)
                        .width(60.dp)
                        .background(
                            color = MaterialTheme.colors.secondary,
                            shape = CircleShape
                        )
                )
            }
    }
}


@ExperimentalComposeUiApi
@Composable
private fun ProfileInformation(model: ThatsAppModel, modifier: Modifier) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    with(model) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = modifier
        ) {
            Row {
                InfoTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    about = "name",
                    icon = Icons.Filled.Person,
                    keyboard = keyboard,
                    focusManager = focusManager,
                    model = model,
                    modifier = Modifier.padding(0.dp, 10.dp, 0.dp, 10.dp).width(325.dp)
                )
            }
            Row {
                InfoTextField(
                    value = profileBio,
                    onValueChange = { profileBio = it },
                    about = "bio",
                    icon = Icons.Filled.Info,
                    keyboard = keyboard,
                    focusManager = focusManager,
                    model = model,
                    modifier = Modifier.padding(0.dp, 10.dp, 0.dp, 0.dp).width(325.dp)
                )
            }
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun InfoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    about: String,
    icon: ImageVector,
    keyboard: SoftwareKeyboardController?,
    focusManager: FocusManager,
    model: ThatsAppModel,
    modifier: Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Enter your $about") },
        trailingIcon = {
            Icon(icon, contentDescription = "Enter your $about")
        },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            autoCorrect = false,
            keyboardType = KeyboardType.Text
        ),
        keyboardActions = KeyboardActions(onDone = {
            keyboard?.hide()
            focusManager.clearFocus()
            model.updatePrefs()
            model.loadOrCreateProfile()
            model.textFieldProfileURL = false
        })
    )
}



