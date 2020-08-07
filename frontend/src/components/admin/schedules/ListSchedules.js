import React, { Component } from "react";
import { Link } from "react-router-dom";
import { Table, Button } from "react-bootstrap";
import { Loading } from "../../shared/Loading";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faMinus, faPlus } from "@fortawesome/free-solid-svg-icons";

function ShowDeletionError({ error }) {
  if (error) {
    return (
      <div>
        <h4>An error ocurred when deleting Schedule {error}</h4>
      </div>
    );
  } else {
    return <div></div>;
  }
}

function RenderData({ isLoading, error, schedules, handleRemove }) {
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
        <h4>An error ocurred when feching Schedules from server: {error}</h4>
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
                <th></th>
              </tr>
            </thead>
            <tbody>
              {schedules.map((schedule) => {
                return (
                  <tr key={schedule.uuid}>
                    <td>
                      {schedule.menu.name}
                      {/* {schedule.menu.dishes.map((dish) => (
                        <p key={dish.uuid}>{dish.name}</p>
                      ))} */}
                    </td>
                    <td>
                      {schedule.location.city}, {schedule.location.country}
                    </td>
                    <td>
                      {schedule.date[2]}-{schedule.date[1]}-{schedule.date[0]}
                    </td>
                    <td>
                      <Button
                        variant="danger"
                        value={schedule.uuid}
                        onClick={() => handleRemove(schedule.uuid)}
                      >
                        <FontAwesomeIcon icon={faMinus} />
                      </Button>
                    </td>
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

class ListSchedules extends Component {
  constructor(props) {
    super(props);
    this.handleRemove = this.handleRemove.bind(this);
  }

  handleRemove(uuid) {
    this.props.deleteSchedule(uuid);
  }

  render() {
    return (
      <div className="container">
        <div>
          <h3 className="mt-4">Management of Schedules</h3>
        </div>
        <Link to={`/newSchedule`}>
          <button type="button" className="btn btn-success">
            <i>
              <FontAwesomeIcon icon={faPlus} />
            </i>{" "}
            New Schedule
          </button>
        </Link>
        <div>
          <RenderData
            isLoading={this.props.isLoading}
            error={this.props.error}
            schedules={this.props.schedules}
            handleRemove={this.handleRemove}
          />
          <ShowDeletionError error={this.props.errorDeleting} />
        </div>
      </div>
    );
  }
}

export default ListSchedules;
