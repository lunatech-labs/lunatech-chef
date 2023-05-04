import { configureStore } from "@reduxjs/toolkit";
import { DishesReducer } from "./dishes/DishesReducer";
import { OfficesReducer } from "./offices/OfficesReducer";
import { MenusReducer } from "./menus/MenusReducer";
import { SchedulesReducer } from "./schedules/SchedulesReducer";
import { UsersReducer } from "./users/UsersReducer";
import { AttendanceReducer } from "./attendance/AttendanceReducer";

export const ConfigureStore = () => {
    const store = configureStore({
        reducer: {
            offices: OfficesReducer,
            dishes: DishesReducer,
            menus: MenusReducer,
            schedules: SchedulesReducer,
            user: UsersReducer,
            attendance: AttendanceReducer,
        }
    });
    return store;
};
