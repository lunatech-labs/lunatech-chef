import React, { Component } from "react";
import { Link, withRouter } from "react-router-dom";
import { Table, Button } from "react-bootstrap";
import { Loading } from "../../shared/Loading";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faMinus, faPlus, faEdit } from "@fortawesome/free-solid-svg-icons";
import { ToMonth } from "../../shared/Functions";

function ShowError({ error, reason }) {
  if (error) {
    return (
      <h4>
        An error ocurred when {reason} a schedule: {error}
      </h4>
    );
  } else {
    return <div></div>;
  }
}

function RenderData({ isLoading, error, schedules, handleEdit, handleRemove }) {
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
                <th></th>
              </tr>
            </thead>
            <tbody>
              {schedules.map((schedule) => {
                return (
                  <tr key={schedule.uuid}>
                    <td>{schedule.menu.name}</td>
                    <td>
                      {schedule.location.city}, {schedule.location.country}
                    </td>
                    <td>
                      {schedule.date[2]} {ToMonth(schedule.date[1])}{" "}
                      {schedule.date[0]}
                    </td>
                    <td>
                      <Button
                        variant="primary"
                        value={schedule.uuid}
                        onClick={() => handleEdit(schedule)}
                      >
                        <FontAwesomeIcon icon={faEdit} />
                      </Button>
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
    this.handleEdit = this.handleEdit.bind(this);
  }

  handleEdit(menu) {
    this.props.history.push("/editschedule", menu);
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
            error={this.props.errorListing}
            schedules={this.props.schedules}
            handleEdit={this.handleEdit}
            handleRemove={this.handleRemove}
          />
          <ShowError error={this.props.errorAdding} reason="adding" />
          <ShowError error={this.props.errorDeleting} reason="deleting" />
          <ShowError error={this.props.errorEditing} reason="saving" />
        </div>
      </div>
    );
  }
}

export default withRouter(ListSchedules);
