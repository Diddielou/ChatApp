package fhnw.emoba.thatsapp.ui.screens

import android.graphics.drawable.Icon
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fhnw.emoba.freezerapp.ui.theme.gray300
import fhnw.emoba.thatsapp.model.Screen
import fhnw.emoba.thatsapp.model.ThatsAppModel
import kotlinx.coroutines.launch

@ExperimentalComposeUiApi
@Composable
fun ProfileScreen(model: ThatsAppModel) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(scaffoldState = scaffoldState,
        topBar = { GeneralTopBar(model, Screen.PROFILE.title, Screen.MAIN, scaffoldState) },
        snackbarHost = { NotificationHost(it) },
        content = { Body(model) }
    )
    Notification(model, scaffoldState)
}

@ExperimentalComposeUiApi
@Composable
private fun Body(model: ThatsAppModel) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(10.dp, 50.dp, 10.dp, 10.dp)
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        ProfilePicture(model)
        ProfileInformation(model, modifier = Modifier.padding(0.dp, 20.dp, 0.dp, 0.dp))
    }
}

@Composable
private fun ProfilePicture(model: ThatsAppModel) {
    with(model) {
        val imageModifier = Modifier
            .size(175.dp)
            .clip(CircleShape)
            .border(1.dp, Color.Transparent, CircleShape)
        Column() {
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
                        imageModifier.background(color = MaterialTheme.colors.secondary.copy(alpha = 0.15f))
                    )
                }
            }
            /*
            Row() {
                IconButton(onClick = { /*TODO*/ },
                    content = { Icon(Icons.Filled.Link, contentDescription = "Insert link") }
                )
                /*
                IconButton(onClick = { /*TODO*/ },
                    content = { Icon(Icons.Filled.PhotoLibrary, contentDescription = "Choose image in library") }
                )
                 */
             */
        }
    }
}


@Composable
@ExperimentalMaterialApi
fun ModalBottomSheet() {
    val state = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    ModalBottomSheetLayout(
        sheetState = state,
        sheetContent = {
            LazyColumn {
                items(4) {

                    ListItem(
                        text = { Text("Get image from link") },
                        icon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete"
                            )
                        },
                        modifier = Modifier.clickable { /* clear image */ }
                    )

                    ListItem(
                        text = { Text("Remove image") },
                        icon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete"
                            )
                        },
                        modifier = Modifier.clickable { /* clear image */ }
                    )

                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Text("Rest of the UI")
            Spacer(Modifier.height(20.dp))
            Button(onClick = { scope.launch { state.show() } }) {
                Text("Click to show sheet")
            }


        }
    }
}


@ExperimentalComposeUiApi
@Composable
private fun ProfileInformation(model: ThatsAppModel, modifier: Modifier) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    with(model) {
        //Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
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
                    modifier = Modifier.padding(0.dp, 10.dp, 0.dp, 10.dp)
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
                    modifier = Modifier.padding(0.dp, 10.dp, 0.dp, 0.dp)
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
            keyboardType = KeyboardType.Ascii
        ),
        keyboardActions = KeyboardActions(onDone = {
            keyboard?.hide()
            focusManager.clearFocus()
            model.updatePrefs()
        })
    )
}