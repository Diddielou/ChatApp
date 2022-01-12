package fhnw.emoba.thatsapp.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fhnw.emoba.freezerapp.ui.theme.gray300
import fhnw.emoba.thatsapp.model.DEFAULT_IMAGE
import fhnw.emoba.thatsapp.model.Screen
import fhnw.emoba.thatsapp.model.ThatsAppModel
import kotlinx.coroutines.CoroutineScope
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
// TODO: Image should be made big onClick
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
            Box(){
                Row() {
                    if (profileImage != null) {
                        Image(
                            bitmap = profileImage!!.asImageBitmap(),
                            contentDescription = "Profile image",
                            modifier = imageModifier
                        )
                    } else {
                        Icon(
                            Icons.Filled.AccountCircle,
                            "Empty profile image",
                            modifier = imageModifier,
                            tint = MaterialTheme.colors.secondary.copy(alpha = 0.3f)
                        )
                    }
                }
                // Show ModalBottomSheet
                IconButton(
                    onClick = { scope.launch { state.show() } },
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
            Row() {
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
            Row() {
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



