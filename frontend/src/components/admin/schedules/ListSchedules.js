import React, { Component } from "react";
import { Link, withRouter } from "react-router-dom";
import { Table, Button } from "react-bootstrap";
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
    this.handleRemove = this.handleRemove.bind(this);
    this.handleDateChange = this.handleDateChange.bind(this);
    this.handleFilter = this.handleFilter.bind(this);

    const savedDate = localStorage.getItem("filterDate");
    const savedLocation = localStorage.getItem("filterLocation");

    this.state = {
      startDate: savedDate === null ? new Date() : new Date(savedDate),
      startLocation: savedLocation === null ? "" : savedLocation,
    };
  }

  handleDateChange = (date) => {
    this.setState({
      startDate: date,
    });
  };

  handleEdit = (schedule) => {
    this.props.history.push("/editschedule", schedule);
  };

  handleRemove = (uuid) => {
    this.props.deleteSchedule(uuid);
  };

  handleFilter = (values) => {
    const shortDate = this.state.startDate.toISOString().substring(0, 10);

    const choosenLocation =
      values.location === undefined ? "" : values.location;

    localStorage.setItem("filterDate", shortDate);
    localStorage.setItem("filterLocation", choosenLocation);
    this.props.filter(shortDate, values.location);
  };

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
              <div className="row">
                <label>Filter by:</label>
              </div>
              <div className="row">
                <Form
                  onSubmit={this.handleFilter}
                  initialValues={{
                    location: this.state.startLocation,
                    date: "", // not used. this.state.startDate used instead
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
                              onClick={() => this.handleRemove(schedule.uuid)}
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
      </div>
    );
  }
}

export default withRouter(ListSchedules);
