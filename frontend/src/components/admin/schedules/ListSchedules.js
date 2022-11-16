import React, { Component } from "react";
import { Link } from "react-router-dom";
import Table from "react-bootstrap/Table";
import Button from "react-bootstrap/Button";
import { Loading } from "../../shared/Loading";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faMinus, faPlus, faEdit } from "@fortawesome/free-solid-svg-icons";
import { ToMonth } from "../../shared/Functions";
import { Form, Field } from "react-final-form";
import DatePicker from "react-datepicker";

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

class ListSchedules extends Component {
  constructor(props) {
    super();
    this.handleEdit = this.handleEdit.bind(this);
    this.handleRemoveSchedule = this.handleRemoveSchedule.bind(this);
    this.handleDateChange = this.handleDateChange.bind(this);
    this.handleFilter = this.handleFilter.bind(this);

    const savedDate = localStorage.getItem("filterDateSchedule");
    const savedLocationSchedule = localStorage.getItem(
      "filterLocationSchedule"
    );

    this.state = {
      startDateSchedule: savedDate === null ? new Date() : new Date(savedDate),
      startLocationSchedule:
        savedLocationSchedule === null ? "" : savedLocationSchedule,
    };
  }

  handleDateChange = (date) => {
    this.setState({
      startDateSchedule: date,
    });
  };

  handleEdit = (schedule) => {
    this.props.history.push("/editschedule", schedule);
  };

  handleRemoveSchedule = (uuid) => {
    this.props.deleteSchedule(uuid);
  };

  handleRemoveRecurrentSchedule = (uuid) => {
    this.props.deleteRecurrentSchedule(uuid);
  };

  handleFilter = (values) => {
    const shortDate = this.state.startDateSchedule
      .toISOString()
      .substring(0, 10);

    const choosenLocation =
      values.location === undefined ? "" : values.location;

    localStorage.setItem("filterDateSchedule", shortDate);
    localStorage.setItem("filterLocationSchedule", choosenLocation);
    this.props.filterSchedules();
    this.props.filterRecurrentSchedules();
  };

  render() {
    return (
      <div className="container">
        <div>
          <h3 className="mt-4">Management of Scheduled Menus</h3>
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
          <ShowError error={this.props.errorAdding} reason="adding" />
          <ShowError error={this.props.errorDeleting} reason="deleting" />
          <ShowError error={this.props.errorEditing} reason="saving" />
          {this.props.isLoading ? (
            <div className="container">
              <div className="row">
                <Loading />
              </div>
            </div>
          ) : (
            <div></div>
          )}
          {this.props.errorListing ? (
            <div>
              <h4>
                An error ocurred when fetching Schedules from server:{" "}
                {this.props.errorListing}
              </h4>
            </div>
          ) : (
            <div className="container">
              <div>
                <h4 className="row">Schedule of Menus</h4>
                <h5 className="mt-4">Where and when a menu will be served</h5>
              </div>
              <div className="row">
                <label>Filter by:</label>
              </div>
              <div className="row">
                <Form
                  onSubmit={this.handleFilter}
                  initialValues={{
                    location: this.state.startLocationSchedule,
                    date: "", // not used. this.state.startDateSchedule used instead
                  }}
                  render={({ handleSubmit, submitting }) => (
                    <form onSubmit={handleSubmit}>
                      <div className="row">
                        <div className="column">
                          <label>Location:</label>
                          <Field name="location" component="select">
                            <option value="" key="" />
                            {this.props.locations.map((location) => {
                              return (
                                <option
                                  value={location.uuid}
                                  key={location.uuid}
                                >
                                  {location.city}, {location.country}
                                </option>
                              );
                            })}
                          </Field>
                        </div>
                        <div className="column">
                          <Field name="date" component="input">
                            {({ input, meta }) => (
                              <div>
                                <label>Date:</label>
                                <DatePicker
                                  selected={this.state.startDate}
                                  onChange={this.handleDateChange}
                                  dateFormat="dd-MM-yyyy"
                                />
                                {meta.error && meta.touched && (
                                  <span>{meta.error}</span>
                                )}
                              </div>
                            )}
                          </Field>
                        </div>
                        <div>
                          <button
                            type="submit"
                            color="primary"
                            disabled={submitting}
                          >
                            Filter
                          </button>
                        </div>
                      </div>
                    </form>
                  )}
                ></Form>
              </div>
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
                    {this.props.schedules.map((schedule) => {
                      return (
                        <tr key={schedule.uuid}>
                          <td>{schedule.menu.name}</td>
                          <td>
                            {schedule.location.city},{" "}
                            {schedule.location.country}
                          </td>
                          <td>
                            {schedule.date[2]} {ToMonth(schedule.date[1])}{" "}
                            {schedule.date[0]}
                          </td>
                          <td>
                            <Button
                              variant="primary"
                              value={schedule.uuid}
                              onClick={() => this.handleEdit(schedule)}
                            >
                              <FontAwesomeIcon icon={faEdit} />
                            </Button>
                          </td>
                          <td>
                            <Button
                              variant="danger"
                              value={schedule.uuid}
                              onClick={() =>
                                this.handleRemoveSchedule(schedule.uuid)
                              }
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
          )}
        </div>
        <div>
          <h4 className="mt-4">Recurrent Schedules</h4>
          <h5 className="mt-4">
            A list of menus that will be scheduled automatically based on it's
            recurrency
          </h5>
        </div>
        <div>
          <ShowError error={this.props.errorAdding} reason="adding" />
          <ShowError error={this.props.errorDeleting} reason="deleting" />
          <ShowError error={this.props.errorEditing} reason="saving" />
          {this.props.isLoading ? (
            <div className="container">
              <div className="row">
                <Loading />
              </div>
            </div>
          ) : (
            <div></div>
          )}
          <div className="container">
            <div className="row">
              <label>Filter by:</label>
            </div>
            <div className="row">
              <Table striped bordered hover>
                <thead>
                  <tr>
                    <th>Menu</th>
                    <th>Location</th>
                    <th>Recurrency (days)</th>
                    <th>Next date</th>
                    <th></th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {this.props.recurrentSchedules.map((recurrentSchedule) => {
                    return (
                      <tr key={recurrentSchedule.uuid}>
                        <td>{recurrentSchedule.menu.name}</td>
                        <td>
                          {recurrentSchedule.location.city},{" "}
                          {recurrentSchedule.location.country}
                        </td>
                        <td>{recurrentSchedule.repetitionDays}</td>
                        <td>
                          {" "}
                          {recurrentSchedule.nextDate[2]}{" "}
                          {ToMonth(recurrentSchedule.nextDate[1])}{" "}
                          {recurrentSchedule.nextDate[0]}
                        </td>
                        <td>
                          <Button
                            variant="primary"
                            value={recurrentSchedule.uuid}
                            onClick={() => this.handleEdit(recurrentSchedule)}
                          >
                            <FontAwesomeIcon icon={faEdit} />
                          </Button>
                        </td>
                        <td>
                          <Button
                            variant="danger"
                            value={recurrentSchedule.uuid}
                            onClick={() =>
                              this.handleRemoveRecurrentSchedule(
                                recurrentSchedule.uuid
                              )
                            }
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
        </div>
      </div>
    );
  }
}

export default ListSchedules;
