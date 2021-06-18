import React, { Component } from "react";
import { withRouter } from "react-router-dom";
import { Table, Button } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faList } from "@fortawesome/free-solid-svg-icons";
import { Loading } from "./shared/Loading";
import { ToMonth } from "./shared/Functions";

function RenderData({ isLoading, error, attendances, handleDetails }) {
  if (isLoading) {
    return (
      <div className="container">
        <div className="row">
          <Loading />
        </div>
      </div>
    );
  } else if (error) {
    return (
      <div>
        <h4>An error ocurred when feching attendances from server: {error}</h4>
      </div>
    );
  } else {
    return (
      <div className="container">
        <div className="row">
          <Table striped bordered hover>
            <thead>
              <tr>
                <th>Menu</th>
                <th>Location</th>
                <th>Date</th>
                <th>Number attendants</th>
                <th>See details</th>
              </tr>
            </thead>
            <tbody>
              {attendances.map((attendance) => {
                return (
                  <tr key={attendance.uuid}>
                    <td>{attendance.menuName}</td>
                    <td>
                      {attendance.location.city}, {attendance.location.country}
                    </td>
                    <td>
                      {attendance.date[2]} {ToMonth(attendance.date[1])}{" "}
                      {attendance.date[0]}
                    </td>
                    <td>{attendance.attendants.length}</td>
                    <Button
                      variant="primary"
                      value={attendance.uuid}
                      onClick={() => handleDetails(attendance.attendants)}
                    >
                      <FontAwesomeIcon icon={faList} />
                    </Button>
                  </tr>
                );
              })}
            </tbody>
          </Table>
        </div>
      </div>
    );
  }
}

class WhoIsJoining extends Component {
  constructor(props) {
    super();
    this.handleDetails = this.handleDetails.bind(this);
  }

  handleDetails(attendants) {
    this.props.history.push("/whoisjoininglisting", attendants);
  }

  render() {
    return (
      <div className="container">
        <div>
          <h3 className="mt-4">Who is joining?</h3>
        </div>
        <div>
          <RenderData
            isLoading={this.props.isLoading}
            error={this.props.errorListing}
            attendances={this.props.attendance}
            handleDetails={this.handleDetails}
          />
        </div>
      </div>
    );
  }
}

export default withRouter(WhoIsJoining);
