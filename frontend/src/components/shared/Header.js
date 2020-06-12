import React, { Component } from "react";
import { Jumbotron } from "react-bootstrap";
import "../../css/simple-sidebar.css";

class Header extends Component {
  render() {
    return (
      <React.Fragment>
        <Jumbotron>
          <div className="sidebar-heading text-dark fluid">
            <h3>Lunatech Chef</h3>
            <p>Planned meals for upcoming Fridays!</p>
          </div>
        </Jumbotron>
      </React.Fragment>
    );
  }
}

export default Header;
