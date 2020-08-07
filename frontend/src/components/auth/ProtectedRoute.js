import React, { Component } from "react";

class ProtectedRoute extends Component {
  render() {
    const Component = this.props.component;
    const isAdmin = this.props.isAdmin;

    return isAdmin ? <Component /> : <div></div>;
  }
}

export default ProtectedRoute;
