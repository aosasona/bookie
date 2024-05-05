package com.trulyao.bookie.views

import androidx.navigation.NavController


public enum class SharedRoutes {
    SignIn,
    SignUp,
    LoadingScreen
}

public enum class UserRoutes {
    Home,
    Activities,
    Profile,
    ChangePassword
}

public enum class AdminRoutes {
    Moderation,
    Students,
    Users,
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
