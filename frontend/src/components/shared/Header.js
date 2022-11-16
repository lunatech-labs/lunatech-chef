import React, { Component } from "react";
import "../../css/simple-sidebar.css";

class Header extends Component {
  render() {
    return (
      <React.Fragment>
        <div className="sidebar-heading text-dark fluid">
          <h3>Lunatech Chef</h3>
          <p>Planned meals for upcoming Fridays!</p>
        </div>
      </React.Fragment>
    );
  }
}

export default Header;
