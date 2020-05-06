import React, { Component } from "react";
import { Switch, Route, Redirect, withRouter, Link } from "react-router-dom";
import { connect } from "react-redux";
import { fetchDishes } from "../redux/ActionCreators";
import "../css/simple-sidebar.css";
import Header from "./Header";
import Footer from "./Footer";
import Dishes from "./Dishes";
import ErrorBoundary from "./ErrorBoundary";

const mapStateToProps = (state) => {
  return {
    dishes: state.dishes,
  };
};

const mapDispatchToProps = (dispatch) => ({
  fetchDishes: () => {
    dispatch(fetchDishes());
  },
});

class Main extends Component {
  componentDidMount() {
    this.props.fetchDishes();
  }

  render() {
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
                to="/alldishes"
              >
                Dishes
              </Link>
            </div>
          </div>
          <Switch>
            {/* do not use the same routes as the ones available in the BE server */}
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
