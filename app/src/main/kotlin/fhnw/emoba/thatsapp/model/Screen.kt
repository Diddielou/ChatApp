package fhnw.emoba.thatsapp.model

enum class Screen(val title: String) {
    MAIN("ThatsApp"), // Overview all users/participants
    ADDCHAT("Add chat"),
    CHAT("Chatting with "),
    PROFILE("Profile")
}