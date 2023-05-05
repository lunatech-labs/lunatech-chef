import { configureStore } from "@reduxjs/toolkit";
import dishesReducer from "./dishes/DishesSlice";
import menusReducer from "./menus/MenusSlice";
import officesReducer from "./offices/OfficesSlice";
import schedulesReducer from "./schedules/SchedulesSlice";
import usersReducer from "./users/UsersSlice";
import attendanceReducer from "./attendance/AttendanceSlice";

export const ConfigureStore = () => {
    const store = configureStore({
        reducer: {
            offices: officesReducer,
            dishes: dishesReducer,
            menus: menusReducer,
            schedules: schedulesReducer,
            user: usersReducer,
            attendance: attendanceReducer,
        },
        middleware: getDefaultMiddleware =>
            getDefaultMiddleware({
                serializableCheck: false,
            }),
    });
    return store;
};
