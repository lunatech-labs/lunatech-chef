import React from "react";
import { Route, Routes, Navigate } from "react-router-dom";
import Container from 'react-bootstrap/Container';
import Sidebar from "./shared/Sidebar";
import { useSelector, useDispatch } from 'react-redux'
import {
    addNewDish,
    editDish,
    deleteDish,
} from "../redux/dishes/DishesActionCreators";
import {
    addNewOffice,
    editOffice,
    deleteOffice,
} from "../redux/offices/OfficesActionCreators";
import {
    addNewMenu,
    editMenu,
    deleteMenu,
} from "../redux/menus/MenusActionCreators";
import {
    fetchSchedules,
    fetchRecurrentSchedules,
    fetchSchedulesAttendance,
    addNewSchedule,
    editSchedule,
    deleteSchedule,
    deleteRecurrentSchedule,
} from "../redux/schedules/SchedulesActionCreators";
import {
    fetchAttendanceUser,
    editAttendance,
    showNewAttendance,
} from "../redux/attendance/AttendanceActionCreators";
import {
    login,
    logout,
    saveUserProfile,
} from "../redux/users/UsersActionCreators";
import {
    getReport,
} from "../redux/reports/ReportsActionCreators";
import { AddDish } from "./admin/dishes/AddDish";
import { EditDish } from "./admin/dishes/EditDish";
import { AddOffice } from "./admin/offices/AddOffice";
import { EditOffice } from "./admin/offices/EditOffice";
import { AddMenu } from "./admin/menus/AddMenu";
import { EditMenu } from "./admin/menus/EditMenu";
import { ListMealsForUser } from "./ListMealsForUser";
import { UserProfile } from "./UserProfile";
import AddSchedule from "./admin/schedules/AddSchedule";
import ErrorBoundary from "./shared/ErrorBoundary";
import EditSchedule from "./admin/schedules/EditSchedule";
import ListDishes from "./admin/dishes/ListDishes";
import ListMenus from "./admin/menus/ListMenus";
import ListOffice from "./admin/offices/ListOffice";
import ListSchedules from "./admin/schedules/ListSchedules";
import MonthlyReports from "./admin/reports/MonthlyReports";
import Login from "./auth/Login";
import WhoIsJoining from "./WhoIsJoining";
import ProtectedRoutes from "./auth/ProtectedRoutes";

function Main() {

    const userState = useSelector(state => state.user)
    const officesState = useSelector(state => state.offices)
    const dishesState = useSelector(state => state.dishes)
    const menusState = useSelector(state => state.menus)
    const schedulesState = useSelector(state => state.schedules)
    const attendanceState = useSelector(state => state.attendance)

    // Create callback functions that dispatch as needed, with arguments
    const dispatch = useDispatch()
    //
    // Offices
    const handleAddNewOffice = (newOffice) => {
        dispatch(addNewOffice(newOffice))
    }
    const handleEditOffice = (editedOffice) => {
        dispatch(editOffice(editedOffice))
    }
    const handleDeleteOffice = (officeUuid) => {
        dispatch(deleteOffice(officeUuid))
    }
    //
    // Dishes
    const handleAddNewDish = (newDish) => {
        dispatch(addNewDish(newDish))
    }
    const handleEditDish = (editedDish) => {
        dispatch(editDish(editedDish))
    }
    const handleDeleteDish = (dishUuid) => {
        dispatch(deleteDish(dishUuid))
    }
    //
    // Menus
    const handleAddNewMenu = (newMenu) => {
        dispatch(addNewMenu(newMenu))
    }
    const handleEditMenu = (editedMenu) => {
        dispatch(editMenu(editedMenu))
    }
    const handleDeleteMenu = (menuUuid) => {
        dispatch(deleteMenu(menuUuid))
    }
    //
    // Schedules
    const handleFetchSchedules = () => {
        dispatch(fetchSchedules())
    }
    const handleAddNewSchedule = (newSchedule) => {
        dispatch(addNewSchedule(newSchedule))
    }
    const handleEditSchedule = (editedSchedule) => {
        dispatch(editSchedule(editedSchedule))
    }
    const handleDeleteSchedule = (scheduleUuid) => {
        dispatch(deleteSchedule(scheduleUuid))
    }
    //
    // Recurrent Schedules
    const handleFetchRecurrentSchedules = () => {
        dispatch(fetchRecurrentSchedules())
    }
    const handleDeleteRecurrentSchedule = (scheduleUuid) => {
        dispatch(deleteRecurrentSchedule(scheduleUuid))
    }
    //
    // Attendance
    const handleFetchAttendanceUser = () => {
        dispatch(fetchAttendanceUser())
    }
    const handleFetchSchedulesAttendance = () => {
        dispatch(fetchSchedulesAttendance())
    }
    const handleEditAttendance = (attendance) => {
        dispatch(editAttendance(attendance))
    }
    const handleShowNewAttendance = (attendance) => {
        dispatch(showNewAttendance(attendance))
    }
    //
    // Reports
    const handleGetReport = (parameters) => {
        dispatch(getReport(parameters))
    }
    //
    // Users
    const handleLogin = (token) => {
        dispatch(login(token))
    }
    const handleLogout = () => {
        dispatch(logout())
    }
    const handleSaveUserProfile = (uuid, profile) => {
        dispatch(saveUserProfile(uuid, profile))
    }


    const WhoIsJoiningSchedule = () => {
        return (
            <WhoIsJoining
                isLoading={schedulesState.isLoadingAttendance}
                attendance={schedulesState.attendance}
                offices={officesState.offices}
                errorListing={schedulesState.errorListingAttendance}
                filter={handleFetchSchedulesAttendance}
            />
        );
    };

    const AllDishes = () => {
        return (
            <ListDishes
                isLoading={dishesState.isLoading}
                dishes={dishesState.dishes}
                editDish={handleEditDish}
                deleteDish={handleDeleteDish}
                errorListing={dishesState.errorListing}
                errorAdding={dishesState.errorAdding}
                errorEditing={dishesState.errorEditing}
                errorDeleting={dishesState.errorDeleting}
            />
        );
    };

    const AddNewDish = () => {
        return <AddDish addNewDish={handleAddNewDish} />;
    };

    const EditExistingDish = () => {
        return (
            <EditDish
                editDish={handleEditDish}
                error={dishesState.errorEditing}
            />
        );
    };

    const AllOffices = () => {
        return (
            <ListOffice
                isLoading={officesState.isLoading}
                offices={officesState.offices}
                editOffice={handleEditOffice}
                deleteOffice={handleDeleteOffice}
                errorListing={officesState.errorListing}
                errorAdding={officesState.errorAdding}
                errorEditing={officesState.errorEditing}
                errorDeleting={officesState.errorDeleting}
            />
        );
    };

    const AddNewOffice = () => {
        return <AddOffice addNewOffice={handleAddNewOffice} />;
    };

    const EditExistingOffice = () => {
        return (
            <EditOffice
                editOffice={handleEditOffice}
                error={officesState.errorEditing}
            />
        );
    };

    const AllMenus = () => {
        return (
            <ListMenus
                isLoading={menusState.isLoading}
                menus={menusState.menus}
                deleteMenu={handleDeleteMenu}
                errorListing={menusState.errorListing}
                errorAdding={menusState.errorAdding}
                errorEditing={menusState.errorEditing}
                errorDeleting={menusState.errorDeleting}
            />
        );
    };

    const AddNewMenu = () => {
        return (
            <AddMenu
                addNewMenu={handleAddNewMenu}
                dishes={dishesState.dishes}
                error={menusState.errorAdding}
            />
        );
    };

    const EditExistingMenu = () => {
        return (
            <EditMenu
                editMenu={handleEditMenu}
                dishes={dishesState.dishes}
                error={menusState.errorEditing}
            />
        );
    };

    const AllSchedules = () => {
        return (
            <ListSchedules
                isLoading={schedulesState.isLoading}
                schedules={schedulesState.schedules}
                recurrentSchedules={schedulesState.recurrentSchedules}
                offices={officesState.offices}
                deleteSchedule={handleDeleteSchedule}
                deleteRecurrentSchedule={handleDeleteRecurrentSchedule}
                errorListing={schedulesState.errorListing}
                errorAdding={schedulesState.errorAdding}
                errorEditing={schedulesState.errorEditing}
                errorDeleting={schedulesState.errorDeleting}
                filterSchedules={handleFetchSchedules}
                filterRecurrentSchedules={handleFetchRecurrentSchedules}
            />
        );
    };

    const AddNewSchedule = () => {
        return (
            <AddSchedule
                addNewSchedule={handleAddNewSchedule}
                menus={menusState.menus}
                offices={officesState.offices}
                error={schedulesState.errorAdding}
            />
        );
    };

    const EditExistingSchedule = () => {
        return (
            <EditSchedule
                editSchedule={handleEditSchedule}
                menus={menusState.menus}
                offices={officesState.offices}
                error={menusState.errorEditing}
            />
        );
    };

    const ListSchedulesForUser = () => {
        return (
            <ListMealsForUser
                isLoading={attendanceState.isLoading}
                attendance={attendanceState.attendance}
                editAttendance={handleEditAttendance}
                showNewAttendance={handleShowNewAttendance}
                errorListing={attendanceState.errorListing}
                offices={officesState.offices}
                filter={handleFetchAttendanceUser}
            />
        );
    };

    const Reports = () => {
        return (<MonthlyReports getReport={handleGetReport} />);
    };


    const LoginUser = () => {
        return <Login login={handleLogin} />;
    };

    const Profile = () => {
        return (
            <UserProfile
                user={userState}
                offices={officesState.offices}
                saveUserProfile={handleSaveUserProfile}
            />
        );
    };

    return (
        <ErrorBoundary>
            {userState.isAuthenticated ? (
                <Container>
                    <div className="d-flex" id="wrapper">
                        <Sidebar logout={handleLogout} isAdmin={userState.isAdmin} />
                        <Routes>
                            {/* do not use the same routes as the ones available in the BE server */}
                            <Route
                                path="/whoisjoining"
                                element={<WhoIsJoiningSchedule />}
                            />
                            <Route element={<ProtectedRoutes isAdmin={userState.isAdmin} />}>
                                <Route path="/alloffices" element={<AllOffices />} />
                                <Route path="/newoffice" element={<AddNewOffice />} />
                                <Route path="/editoffice" element={<EditExistingOffice />} />

                                <Route path="/alldishes" element={<AllDishes />} />
                                <Route path="/newdish" element={<AddNewDish />} />
                                <Route path="/editdish" element={<EditExistingDish />} />

                                <Route path="/allmenus" element={<AllMenus />} />
                                <Route path="/newmenu" element={<AddNewMenu />} />
                                <Route path="/editmenu" element={<EditExistingMenu />} />

                                <Route path="/allschedules" element={<AllSchedules />} />
                                <Route path="/newschedule" element={<AddNewSchedule />} />
                                <Route path="/editschedule" element={<EditExistingSchedule />} />
                                <Route path="/monthlyreports" element={<Reports />} />

                            </Route>
                            <Route path="/loginUser" element={<LoginUser />} />
                            <Route path="/userProfile" element={<Profile />} />
                            <Route path="/" element={<ListSchedulesForUser />} />
                            <Route
                                path="*"
                                element={<Navigate to="/" replace />}
                            />
                        </Routes>
                    </div>
                </Container>
            ) : (
                <Container>
                    <div className="d-flex" id="wrapper">
                        <LoginUser />
                    </div>
                </Container>
            )
            }
        </ErrorBoundary>
    );
}

export default Main;