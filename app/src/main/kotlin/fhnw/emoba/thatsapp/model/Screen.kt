package fhnw.emoba.thatsapp.model

enum class Screen(val title: String) {
    MAIN("ThatsApp"), // Overview all users/participants
    ADDUSER("Add user"),
    CHAT("Chatting with "),
    PROFILE("Profile")
}