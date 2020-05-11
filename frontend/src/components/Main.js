import React, { Component } from "react";
import { Switch, Route, Redirect, withRouter, Link } from "react-router-dom";
import { connect } from "react-redux";
import { actions } from "react-redux-form";
import { fetchDishes } from "../redux/dishes/DishesActionCreators";
import {
  fetchLocations,
  addNewlocation,
  deleteLocation,
} from "../redux/locations/LocationActionCreators";
import "../css/simple-sidebar.css";
import Header from "./shared/Header";
import Footer from "./shared/Footer";
import ErrorBoundary from "./shared/ErrorBoundary";
import Dishes from "./admin/Dishes";
import ListLocations from "./admin/locations/ListLocations";
import AddLocation from "./admin/locations/AddLocation";

const mapStateToProps = (state) => {
  return {
    locations: state.locations,
    dishes: state.dishes,
  };
};

const mapDispatchToProps = (dispatch) => ({
  fetchDishes: () => {
    dispatch(fetchDishes());
  },
  fetchLocations: () => {
    dispatch(fetchLocations());
  },
  addNewlocation: (newLocation) => {
    dispatch(addNewlocation(newLocation));
  },
  resetNewLocationForm: () => {
    dispatch(actions.reset("newLocation"));
  },
  deleteLocation: (locationUuid) => {
    dispatch(deleteLocation(locationUuid));
  },
});

class Main extends Component {
  componentDidMount() {
    this.props.fetchLocations();
    this.props.fetchDishes();
  }

  render() {
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
          addNewlocation={this.props.addNewlocation}
          resetNewLocationForm={this.props.resetNewLocationForm}
        />
      );
    };

    const AllDishes = () => {
      return (
        <Dishes
          isLoading={this.props.dishes.isLoading}
          error={this.props.dishes.error}
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
            </div>
          </div>
          <Switch>
            {/* do not use the same routes as the ones available in the BE server */}
            <Route path="/alllocations" component={AllLocations} />
            <Route path="/newLocation" component={AddNewLocation} />
            <Route path="/alldishes" component={AllDishes} />
            <Redirect to="/" />
          </Switch>
        </div>
        <Footer />
      </ErrorBoundary>
    );
  }
}

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(Main));
