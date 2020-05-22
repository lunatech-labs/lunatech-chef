import React, { Component } from "react";
import { Switch, Route, Redirect, withRouter, Link } from "react-router-dom";
import { connect } from "react-redux";
import {
  fetchDishes,
  addNewDish,
  deleteDish,
} from "../redux/dishes/DishesActionCreators";
import {
  fetchLocations,
  addNewLocation,
  deleteLocation,
} from "../redux/locations/LocationsActionCreators";
import {
  fetchMenus,
  addNewMenu,
  deleteMenu,
} from "../redux/menus/MenusActionCreators";
import "../css/simple-sidebar.css";
import Header from "./shared/Header";
import Footer from "./shared/Footer";
import ErrorBoundary from "./shared/ErrorBoundary";
import ListDishes from "./admin/dishes/ListDishes";
import { AddDish } from "./admin/dishes/AddDish";
import ListLocations from "./admin/locations/ListLocations";
import { AddLocation } from "./admin/locations/AddLocation";
import ListMenus from "./admin/menus/ListMenus";
import { AddMenu } from "./admin/menus/AddMenu";

const mapStateToProps = (state) => {
  return {
    locations: state.locations,
    dishes: state.dishes,
    menus: state.menus,
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
  deleteMenu: (menuUuid) => {
    dispatch(deleteMenu(menuUuid));
  },
});

class Main extends Component {
  componentDidMount() {
    this.props.fetchLocations();
    this.props.fetchDishes();
    this.props.fetchMenus();
  }

  render() {
    const AllDishes = () => {
      return (
        <ListDishes
          isLoading={this.props.dishes.isLoading}
          error={this.props.dishes.error}
          dishes={this.props.dishes.dishes.data}
          deleteDish={this.props.deleteDish}
        />
      );
    };

    const AddNewDish = () => {
      return (
        <AddDish
          addNewDish={this.props.addNewDish}
          resetNewDishForm={this.props.resetNewDishForm}
        ></AddDish>
      );
    };

    const AllLocations = () => {
      return (
        <ListLocations
          isLoading={this.props.locations.isLoading}
          error={this.props.locations.error}
          locations={this.props.locations.locations.data}
          deleteLocation={this.props.deleteLocation}
        />
      );
    };

    const AddNewLocation = () => {
      return (
        <AddLocation
          addNewLocation={this.props.addNewLocation}
          resetNewLocationForm={this.props.resetNewLocationForm}
        />
      );
    };

    const AllMenus = () => {
      return (
        <ListMenus
          isLoading={this.props.menus.isLoading}
          error={this.props.menus.error}
          menus={this.props.menus.menus.data}
          deleteMenu={this.props.deleteMenu}
        />
      );
    };

    const AddNewMenu = () => {
      return (
        <AddMenu
          addNewMenu={this.props.addNewMenu}
          resetNewMenuForm={this.props.resetNewMenuForm}
          dishes={this.props.dishes.dishes.data}
        />
      );
    };

    return (
      <ErrorBoundary>
        <div className="d-flex" id="wrapper">
          <div className="bg-light border-right" id="sidebar-wrapper">
            <Header />
            <div className="list-group list-group-flush">
              <Link
                className="list-group-item list-group-item-action bg-light"
                to="/"
              >
                Home
              </Link>
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
            </div>
          </div>
          <Switch>
            {/* do not use the same routes as the ones available in the BE server */}
            <Route path="/alllocations" component={AllLocations} />
            <Route path="/newLocation" component={AddNewLocation} />
            <Route path="/alldishes" component={AllDishes} />
            <Route path="/newdish" component={AddNewDish} />
            <Route path="/allMenus" component={AllMenus} />
            <Route path="/newMenu" component={AddNewMenu} />
            <Redirect to="/" />
          </Switch>
        </div>
        <Footer />
      </ErrorBoundary>
    );
  }
}

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(Main));
