import { configureStore } from "@reduxjs/toolkit";
import dishesReducer from "./dishes/DishesSlice";
import menusReducer from "./menus/MenusSlice";
import officesReducer from "./offices/OfficesSlice";
import { SchedulesReducer } from "./schedules/SchedulesReducer";
import { UsersReducer } from "./users/UsersReducer";
import { AttendanceReducer } from "./attendance/AttendanceReducer";

export const ConfigureStore = () => {
    const store = configureStore({
        reducer: {
            offices: officesReducer,
            dishes: dishesReducer,
            menus: menusReducer,
            schedules: SchedulesReducer,
            user: UsersReducer,
            attendance: AttendanceReducer,
        },
        middleware: getDefaultMiddleware =>
            getDefaultMiddleware({
                serializableCheck: false,
            }),
    });
    return store;
};
