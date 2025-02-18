package com.example.grimmy.Retrofit.Response

data class GoalGetResponse(
    val monthly_goals : List<MonthlyGoal>,
    val weekly_goals : List<WeeklyGoal>,
    val stuff : List<Stuff>
)

data class MonthlyGoal(
    val goal_id : Int,
    val title : String,
    val subgoals : List<SubGoal>
)

data class WeeklyGoal(
    val goal_id : Int,
    val title : String,
    val subgoals : List<SubGoal>
)

data class SubGoal(
    val subgoal_id : Int,
    val title : String,
    val is_completed : Boolean
)

data class Stuff(
    val stuff_id : Int,
    val title: String,
    val substuffs : List<SubStuff>
)

data class SubStuff(
    val substuff_id : Int,
    val title : String,
    val is_completed: Boolean
)
