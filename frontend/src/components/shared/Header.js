import React, { Component } from "react";
import "../../css/simple-sidebar.css";
import Row from 'react-bootstrap/Row';
import { Col } from "react-bootstrap";

class Header extends Component {
  render() {
    return (
      <React.Fragment>
        <Row className="sidebar-heading text-dark fluid">
          <Col>
            <h3>Lunatech Chef</h3>
          </Col>
          <Row>
            <Col>Planned meals for upcoming Fridays!
            </Col>
          </Row>
        </Row>
      </React.Fragment>
    );
  }
}

export default Header;
