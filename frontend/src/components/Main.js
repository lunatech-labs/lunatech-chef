import React, { Component } from "react";
import { Route, Routes, Navigate, Link } from "react-router-dom";
import Button from "react-bootstrap/Button";
import Header from "./shared/Header";
import Footer from "./shared/Footer";
import { connect } from "react-redux";
import {
  fetchDishes,
  addNewDish,
  editDish,
  deleteDish,
} from "../redux/dishes/DishesActionCreators";
import {
  fetchLocations,
  addNewLocation,
  editLocation,
  deleteLocation,
} from "../redux/locations/LocationsActionCreators";
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
import "../css/simple-sidebar.css";
import { AddDish } from "./admin/dishes/AddDish";
import { EditDish } from "./admin/dishes/EditDish";
import { AddLocation } from "./admin/locations/AddLocation";
import { EditLocation } from "./admin/locations/EditLocation";
import { AddMenu } from "./admin/menus/AddMenu";
import { EditMenu } from "./admin/menus/EditMenu";
import { MealsAttendance } from "./MealsSchedule";
import { WhoIsJoiningListing } from "./WhoIsJoiningListing";
import { UserProfile } from "./UserProfile";
import AddSchedule from "./admin/schedules/AddSchedule";
import ErrorBoundary from "./shared/ErrorBoundary";
import EditSchedule from "./admin/schedules/EditSchedule";
import ListDishes from "./admin/dishes/ListDishes";
import ListMenus from "./admin/menus/ListMenus";
import ListLocations from "./admin/locations/ListLocations";
import ListSchedules from "./admin/schedules/ListSchedules";
import Login from "./auth/Login";
import WhoIsJoining from "./WhoIsJoining";
import ProtectedRoutes from "./auth/ProtectedRoutes";

const mapStateToProps = (state) => {
  return {
    user: state.user,
    locations: state.locations,
    dishes: state.dishes,
    menus: state.menus,
    schedules: state.schedules,
    attendance: state.attendance,
  };
};

const mapDispatchToProps = (dispatch) => ({
  //
  // Locations
  fetchLocations: () => {
    dispatch(fetchLocations());
  },
  addNewLocation: (newLocation) => {
    dispatch(addNewLocation(newLocation));
  },
  editLocation: (editedLocation) => {
    dispatch(editLocation(editedLocation));
  },
  deleteLocation: (locationUuid) => {
    dispatch(deleteLocation(locationUuid));
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
          locations={this.props.locations.locations}
          errorListing={this.props.schedules.errorListingAttendance}
          filter={this.props.fetchSchedulesAttendance}
        />
      );
    };

    const WhoIsJoiningScheduleList = () => {
      return <WhoIsJoiningListing listAttendants={this.props.location.state} />;
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

    const AllLocations = () => {
      return (
        <ListLocations
          isLoading={this.props.locations.isLoading}
          locations={this.props.locations.locations}
          editLocation={this.props.editLocation}
          deleteLocation={this.props.deleteLocation}
          errorListing={this.props.locations.errorListing}
          errorAdding={this.props.locations.errorAdding}
          errorEditing={this.props.locations.errorEditing}
          errorDeleting={this.props.locations.errorDeleting}
        />
      );
    };

    const AddNewLocation = () => {
      return <AddLocation addNewLocation={this.props.addNewLocation} />;
    };

    const EditExistingLocation = () => {
      return (
        <EditLocation
          editLocation={this.props.editLocation}
          error={this.props.locations.errorEditing}
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
          menu={this.props.location.state}
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
          locations={this.props.locations.locations}
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
          locations={this.props.locations.locations}
          error={this.props.schedules.errorAdding}
        />
      );
    };

    const EditExistingSchedule = () => {
      return (
        <EditSchedule
          editSchedule={this.props.editSchedule}
          schedule={this.props.location.state}
          menus={this.props.menus.menus}
          locations={this.props.locations.locations}
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
          locations={this.props.locations.locations}
          filter={this.props.fetchAttendanceUser}
        />
      );
    };

    const LoginUser = () => {
      return <Login login={this.props.login} />;
    };

    const Profile = () => {
      return (
        <UserProfile
          user={this.props.user}
          locations={this.props.locations.locations}
          saveUserProfile={this.props.saveUserProfile}
        />
      );
    };

    return (
      <ErrorBoundary>
        {this.props.user.isAuthenticated ? (
          <div className="d-flex" id="wrapper">
            <div className="bg-light border-right" id="sidebar-wrapper">
              <Header />
              <div className="list-group list-group-flush">
                <Link
                  className="list-group-item list-group-item-action bg-light"
                  to="/"
                >
                  Meals schedule
                </Link>
                <Link
                  className="list-group-item list-group-item-action bg-light"
                  to="/whoisjoining"
                >
                  Who is joining?
                </Link>
                {this.props.user.isAdmin ? (
                  <div>
                    <Link
                      className="list-group-item list-group-item-action bg-light"
                      to="/alllocations"
                    >
                      Locations
                    </Link>
                    <Link
                      className="list-group-item list-group-item-action bg-light"
                      to="/alldishes"
                    >
                      Dishes
                    </Link>
                    <Link
                      className="list-group-item list-group-item-action bg-light"
                      to="/allmenus"
                    >
                      Menus
                    </Link>
                    <Link
                      className="list-group-item list-group-item-action bg-light"
                      to="/allschedules"
                    >
                      Schedules
                    </Link>
                  </div>
                ) : (
                  <div></div>
                )}
                <div className="list-group-item list-group-item-action bg-light">
                  {this.props.user.name}
                </div>
                <Link
                  className="list-group-item list-group-item-action bg-light"
                  to="/userProfile"
                >
                  Profile
                </Link>
                <Link to="/">
                  <Button
                    className="list-group-item list-group-item-action bg-light"
                    onClick={this.props.logout}
                  >
                    <span>Logout</span>
                  </Button>
                </Link>
              </div>
            </div>
            <Routes>
              {/* do not use the same routes as the ones available in the BE server */}
              <Route
                path="/whoisjoining"
                element={<WhoIsJoiningSchedule />}
              />
              <Route
                path="/whoisjoininglisting"
                element={<WhoIsJoiningScheduleList />}
              />
              <Route element={<ProtectedRoutes isAdmin={this.props.user.isAdmin} />}>
                <Route path="/alllocations" element={<AllLocations />} />
                <Route path="/newlocation" element={<AddNewLocation />} />
                <Route path="/editlocation" element={<EditExistingLocation />} />

                <Route path="/alldishes" element={<AllDishes />} />
                <Route path="/newdish" element={<AddNewDish />} />
                <Route path="/editdish" element={<EditExistingDish />} />

                <Route path="/allmenus" element={<AllMenus />} />
                <Route path="/newmenu" element={<AddNewMenu />} />
                <Route path="/editmenu" element={<EditExistingMenu />} />

                <Route path="/allschedules" element={<AllSchedules />} />
                <Route path="/newschedule" element={<AddNewSchedule />} />
                <Route path="/editschedule" element={<EditExistingSchedule />} />

              </Route>
              <Route path="/loginUser" element={<LoginUser />} />
              <Route path="/userProfile" element={<Profile />} />
              <Route path="/" element={<ShowAttendance />} />
              <Route
                path="*"
                element={<Navigate to="/" replace />}
              />
            </Routes>
            <div className="d-flex">
              <Footer />
            </div>
          </div>
        ) : (
          <div className="d-flex" id="wrapper">
            <LoginUser />
          </div>
        )
        }
      </ErrorBoundary>
    );
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(Main);
