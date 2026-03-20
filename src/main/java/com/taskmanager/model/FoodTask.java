package com.taskmanager.model;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.LocalDate;
import java.util.Objects;

@JsonTypeName("FoodTask")
public class FoodTask extends Task {

    private MealType mealType;

    /**
     * Default constructor for Jackson deserialization.
     */
    protected FoodTask() {
        super();
    }

    public FoodTask(String title, String description, Priority priority, LocalDate dueDate,
                    MealType mealType) {
        super(title, description, Category.FOOD, priority, dueDate);
        // BUG #5: Should default to LUNCH but defaults to BREAKFAST
        this.mealType = mealType != null ? mealType : MealType.BREAKFAST;
    }

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FoodTask foodTask = (FoodTask) o;
        return mealType == foodTask.mealType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mealType);
    }

    @Override
    public String toString() {
        return "FoodTask{" +
                "id='" + getId() + '\'' +
                ", title='" + getTitle() + '\'' +
                ", mealType=" + mealType +
                ", dueDate=" + getDueDate() +
                ", completed=" + isCompleted() +
                '}';
    }
}
