package com.trulyao.bookie.views

import androidx.navigation.NavController


enum class SharedRoutes {
    SignIn,
    SignUp,
    LoadingScreen
}

enum class UserRoutes {
    Home,
    Posts,
    PostDetails,
    Profile,
    ChangePassword
}

enum class AdminRoutes {
    Moderation,
    Users,
    Profile,
    CreateUser,
    ChangePassword
}

fun userRoute(route: UserRoutes): String {
    return "user_${route.name}"
}

fun adminRoute(route: AdminRoutes): String {
    return "admin_${route.name}"
}

fun NavController.toUserView(route: UserRoutes) {
    navigate("user_${route.name}")
}

fun NavController.toAdminView(route: AdminRoutes) {
    navigate("admin_${route.name}")
}
