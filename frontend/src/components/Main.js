import React, { Component } from "react";
import { Switch, Route, Redirect, withRouter, Link } from "react-router-dom";
import { Button } from "react-bootstrap";
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
  fetchSchedulesAttendance,
  addNewSchedule,
  editSchedule,
  deleteSchedule,
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
import ErrorBoundary from "./shared/ErrorBoundary";
import ListDishes from "./admin/dishes/ListDishes";
import { AddDish } from "./admin/dishes/AddDish";
import { EditDish } from "./admin/dishes/EditDish";
import ListLocations from "./admin/locations/ListLocations";
import { AddLocation } from "./admin/locations/AddLocation";
import { EditLocation } from "./admin/locations/EditLocation";
import ListMenus from "./admin/menus/ListMenus";
import { AddMenu } from "./admin/menus/AddMenu";
import { EditMenu } from "./admin/menus/EditMenu";
import Login from "./auth/Login";
import ProtectedRoute from "./auth/ProtectedRoute";
import ListSchedules from "./admin/schedules/ListSchedules";
import AddSchedule from "./admin/schedules/AddSchedule";
import EditSchedule from "./admin/schedules/EditSchedule";
import { MealsAttendance } from "./MealsSchedule";
import WhoIsJoining from "./WhoIsJoining";
import { WhoIsJoiningListing } from "./WhoIsJoiningListing";
import { UserProfile } from "./UserProfile";

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
          errorListing={this.props.schedules.errorListingAttendance}
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
          dish={this.props.location.state}
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
          location={this.props.location.state}
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
          locations={this.props.locations.locations}
          deleteSchedule={this.props.deleteSchedule}
          errorListing={this.props.schedules.errorListing}
          errorAdding={this.props.schedules.errorAdding}
          errorEditing={this.props.schedules.errorEditing}
          errorDeleting={this.props.schedules.errorDeleting}
          filter={this.props.fetchSchedules}
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
            <Switch>
              {/* do not use the same routes as the ones available in the BE server */}
              <ProtectedRoute
                path="/whoisjoining"
                component={WhoIsJoiningSchedule}
                isAdmin={this.props.user.isAdmin}
              />
              <ProtectedRoute
                path="/whoisjoininglisting"
                component={WhoIsJoiningScheduleList}
                isAdmin={this.props.user.isAdmin}
              />

              <ProtectedRoute
                path="/alllocations"
                component={AllLocations}
                isAdmin={this.props.user.isAdmin}
              />
              <ProtectedRoute
                path="/newlocation"
                component={AddNewLocation}
                isAdmin={this.props.user.isAdmin}
              />
              <ProtectedRoute
                path="/editlocation"
                component={EditExistingLocation}
                isAdmin={this.props.user.isAdmin}
              />
              <ProtectedRoute
                path="/alldishes"
                component={AllDishes}
                isAdmin={this.props.user.isAdmin}
              />
              <ProtectedRoute
                path="/newdish"
                component={AddNewDish}
                isAdmin={this.props.user.isAdmin}
              />
              <ProtectedRoute
                path="/editdish"
                component={EditExistingDish}
                isAdmin={this.props.user.isAdmin}
              />
              <ProtectedRoute
                path="/allmenus"
                component={AllMenus}
                isAdmin={this.props.user.isAdmin}
              />
              <ProtectedRoute
                path="/newmenu"
                component={AddNewMenu}
                isAdmin={this.props.user.isAdmin}
              />
              <ProtectedRoute
                path="/editmenu"
                component={EditExistingMenu}
                isAdmin={this.props.user.isAdmin}
              />
              <ProtectedRoute
                path="/allschedules"
                component={AllSchedules}
                isAdmin={this.props.user.isAdmin}
              />
              <ProtectedRoute
                path="/newschedule"
                component={AddNewSchedule}
                isAdmin={this.props.user.isAdmin}
              />
              <ProtectedRoute
                path="/editschedule"
                component={EditExistingSchedule}
                isAdmin={this.props.user.isAdmin}
              />
              <Route path="/loginUser" component={LoginUser} />
              <Route path="/userProfile" component={Profile} />
              <Route path="/" component={ShowAttendance} />
              <Redirect to="/" />
            </Switch>
            <div className="d-flex">
              <Footer />
            </div>
          </div>
        ) : (
          <div className="d-flex" id="wrapper">
            <LoginUser />
          </div>
        )}
      </ErrorBoundary>
    );
  }
}

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(Main));
