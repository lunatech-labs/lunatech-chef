import React, { Component } from "react";
import { Route, Routes, Navigate } from "react-router-dom";
import Container from 'react-bootstrap/Container';
import Sidebar from "./shared/Sidebar";
import { connect } from "react-redux";
import {
    fetchDishes,
    addNewDish,
    editDish,
    deleteDish,
} from "../redux/dishes/DishesActionCreators";
import {
    fetchOffices,
    addNewOffice,
    editOffice,
    deleteOffice,
} from "../redux/offices/OfficesActionCreators";
import {
    fetchMenus,
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
import { MealsAttendance } from "./ScheduledMeals";
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

const mapStateToProps = (state) => {
    return {
        user: state.user,
        offices: state.offices,
        dishes: state.dishes,
        menus: state.menus,
        schedules: state.schedules,
        attendance: state.attendance,
    };
};

const mapDispatchToProps = (dispatch) => ({
    //
    // Offices
    fetchOffices: () => {
        dispatch(fetchOffices());
    },
    addNewOffice: (newOffice) => {
        dispatch(addNewOffice(newOffice));
    },
    editOffice: (editedOffice) => {
        dispatch(editOffice(editedOffice));
    },
    deleteOffice: (officeUuid) => {
        dispatch(deleteOffice(officeUuid));
    },
    //
    // Dishes
    fetchDishes: () => {
        dispatch(fetchDishes());
    },
    addNewDish: (newDish) => {
        dispatch(addNewDish(newDish));
    },
    editDish: (editedDish) => {
        dispatch(editDish(editedDish));
    },
    deleteDish: (dishUuid) => {
        dispatch(deleteDish(dishUuid));
    },
    //
    // Menus
    fetchMenus: () => {
        dispatch(fetchMenus());
    },
    addNewMenu: (newMenu) => {
        dispatch(addNewMenu(newMenu));
    },
    editMenu: (editedMenu) => {
        dispatch(editMenu(editedMenu));
    },
    deleteMenu: (menuUuid) => {
        dispatch(deleteMenu(menuUuid));
    },
    //
    // Schedules
    fetchSchedules: () => {
        dispatch(fetchSchedules());
    },
    addNewSchedule: (newSchedule) => {
        dispatch(addNewSchedule(newSchedule));
    },
    editSchedule: (editedSchedule) => {
        dispatch(editSchedule(editedSchedule));
    },
    deleteSchedule: (scheduleUuid) => {
        dispatch(deleteSchedule(scheduleUuid));
    },
    //
    // Recurrent Schedules
    fetchRecurrentSchedules: () => {
        dispatch(fetchRecurrentSchedules());
    },
    deleteRecurrentSchedule: (scheduleUuid) => {
        dispatch(deleteRecurrentSchedule(scheduleUuid));
    },
    //
    // Attendance
    fetchAttendanceUser: () => {
        dispatch(fetchAttendanceUser());
    },
    fetchSchedulesAttendance: () => {
        dispatch(fetchSchedulesAttendance());
    },
    editAttendance: (attendance) => {
        dispatch(editAttendance(attendance));
    },
    showNewAttendance: (attendance) => {
        dispatch(showNewAttendance(attendance));
    },
    //
    // Reports
    getReport: (parameters) => {
        dispatch(getReport(parameters));
    },
    //
    // Users
    login: (token) => {
        dispatch(login(token));
    },
    logout: () => {
        dispatch(logout());
    },
    saveUserProfile: (uuid, profile) => {
        dispatch(saveUserProfile(uuid, profile));
    },
});

class Main extends Component {
    render() {
        const WhoIsJoiningSchedule = () => {
            return (
                <WhoIsJoining
                    isLoading={this.props.schedules.isLoadingAttendance}
                    attendance={this.props.schedules.attendance}
                    offices={this.props.offices.offices}
                    errorListing={this.props.schedules.errorListingAttendance}
                    filter={this.props.fetchSchedulesAttendance}
                />
            );
        };

        const AllDishes = () => {
            return (
                <ListDishes
                    isLoading={this.props.dishes.isLoading}
                    dishes={this.props.dishes.dishes}
                    editDish={this.props.editDish}
                    deleteDish={this.props.deleteDish}
                    errorListing={this.props.dishes.errorListing}
                    errorAdding={this.props.dishes.errorAdding}
                    errorEditing={this.props.dishes.errorEditing}
                    errorDeleting={this.props.dishes.errorDeleting}
                />
            );
        };

        const AddNewDish = () => {
            return <AddDish addNewDish={this.props.addNewDish} />;
        };

        const EditExistingDish = () => {
            return (
                <EditDish
                    editDish={this.props.editDish}
                    error={this.props.dishes.errorEditing}
                />
            );
        };

        const AllOffices = () => {
            return (
                <ListOffice
                    isLoading={this.props.offices.isLoading}
                    offices={this.props.offices.offices}
                    editOffice={this.props.editOffice}
                    deleteOffice={this.props.deleteOffice}
                    errorListing={this.props.offices.errorListing}
                    errorAdding={this.props.offices.errorAdding}
                    errorEditing={this.props.offices.errorEditing}
                    errorDeleting={this.props.offices.errorDeleting}
                />
            );
        };

        const AddNewOffice = () => {
            return <AddOffice addNewOffice={this.props.addNewOffice} />;
        };

        const EditExistingOffice = () => {
            return (
                <EditOffice
                    editOffice={this.props.editOffice}
                    error={this.props.offices.errorEditing}
                />
            );
        };

        const AllMenus = () => {
            return (
                <ListMenus
                    isLoading={this.props.menus.isLoading}
                    menus={this.props.menus.menus}
                    deleteMenu={this.props.deleteMenu}
                    errorListing={this.props.menus.errorListing}
                    errorAdding={this.props.menus.errorAdding}
                    errorEditing={this.props.menus.errorEditing}
                    errorDeleting={this.props.menus.errorDeleting}
                />
            );
        };

        const AddNewMenu = () => {
            return (
                <AddMenu
                    addNewMenu={this.props.addNewMenu}
                    dishes={this.props.dishes.dishes}
                    error={this.props.menus.errorAdding}
                />
            );
        };

        const EditExistingMenu = () => {
            return (
                <EditMenu
                    editMenu={this.props.editMenu}
                    dishes={this.props.dishes.dishes}
                    error={this.props.menus.errorEditing}
                />
            );
        };

        const AllSchedules = () => {
            return (
                <ListSchedules
                    isLoading={this.props.schedules.isLoading}
                    schedules={this.props.schedules.schedules}
                    recurrentSchedules={this.props.schedules.recurrentSchedules}
                    offices={this.props.offices.offices}
                    deleteSchedule={this.props.deleteSchedule}
                    deleteRecurrentSchedule={this.props.deleteRecurrentSchedule}
                    errorListing={this.props.schedules.errorListing}
                    errorAdding={this.props.schedules.errorAdding}
                    errorEditing={this.props.schedules.errorEditing}
                    errorDeleting={this.props.schedules.errorDeleting}
                    filterSchedules={this.props.fetchSchedules}
                    filterRecurrentSchedules={this.props.fetchRecurrentSchedules}
                />
            );
        };

        const AddNewSchedule = () => {
            return (
                <AddSchedule
                    addNewSchedule={this.props.addNewSchedule}
                    menus={this.props.menus.menus}
                    offices={this.props.offices.offices}
                    error={this.props.schedules.errorAdding}
                />
            );
        };

        const EditExistingSchedule = () => {
            return (
                <EditSchedule
                    editSchedule={this.props.editSchedule}
                    menus={this.props.menus.menus}
                    offices={this.props.offices.offices}
                    error={this.props.menus.errorEditing}
                />
            );
        };

        const ShowAttendance = () => {
            return (
                <MealsAttendance
                    isLoading={this.props.attendance.isLoading}
                    attendance={this.props.attendance.attendance}
                    editAttendance={this.props.editAttendance}
                    showNewAttendance={this.props.showNewAttendance}
                    errorListing={this.props.attendance.errorListing}
                    offices={this.props.offices.offices}
                    filter={this.props.fetchAttendanceUser}
                />
            );
        };

        const Reports = () => {
            return (<MonthlyReports
                getReport={this.props.getReport} />);
        };


        const LoginUser = () => {
            return <Login login={this.props.login} />;
        };

        const Profile = () => {
            return (
                <UserProfile
                    user={this.props.user}
                    offices={this.props.offices.offices}
                    saveUserProfile={this.props.saveUserProfile}
                />
            );
        };

        return (
            <ErrorBoundary>
                {this.props.user.isAuthenticated ? (
                    <Container>
                        <div className="d-flex" id="wrapper">
                            <Sidebar logout={this.props.logout} isAdmin={this.props.user.isAdmin} />
                            <Routes>
                                {/* do not use the same routes as the ones available in the BE server */}
                                <Route
                                    path="/whoisjoining"
                                    element={<WhoIsJoiningSchedule />}
                                />
                                <Route element={<ProtectedRoutes isAdmin={this.props.user.isAdmin} />}>
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
                                <Route path="/" element={<ShowAttendance />} />
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
}

export default connect(mapStateToProps, mapDispatchToProps)(Main);
