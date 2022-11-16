import React, { Component } from "react";
import Table from "react-bootstrap/Table";
import Button from "react-bootstrap/Button";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faList } from "@fortawesome/free-solid-svg-icons";
import { Loading } from "./shared/Loading";
import { ToMonth } from "./shared/Functions";
import { Form, Field } from "react-final-form";
import DatePicker from "react-datepicker";

function RenderData({
  isLoading,
  error,
  attendances,
  handleDetails,
  handleFilter,
  handleDateChange,
  state,
  locations,
}) {
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
          <label>Filter by:</label>
        </div>
        <div className="row">
          <Form
            onSubmit={handleFilter}
            initialValues={{
              location: state.startLocation,
              date: "", // not used. this.state.startDate used instead
            }}
            render={({ handleSubmit, submitting }) => (
              <form onSubmit={handleSubmit}>
                <div className="row">
                  <div className="column">
                    <label>Location:</label>
                    <Field name="location" component="select">
                      <option value="" key="" />
                      {locations.map((location) => {
                        return (
                          <option value={location.uuid} key={location.uuid}>
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
                            selected={state.startDate}
                            onChange={handleDateChange}
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
                    <button type="submit" color="primary" disabled={submitting}>
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
                    <td>
                      <Button
                        variant="primary"
                        value={attendance.uuid}
                        onClick={() => handleDetails(attendance.attendants)}
                      >
                        <FontAwesomeIcon icon={faList} />
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

class WhoIsJoining extends Component {
  constructor(props) {
    super();
    this.handleDetails = this.handleDetails.bind(this);
    this.handleDateChange = this.handleDateChange.bind(this);
    this.handleFilter = this.handleFilter.bind(this);

    const savedDate = localStorage.getItem("filterDateWhoIsJoining");
    const savedLocation = localStorage.getItem("filterLocationWhoIsJoining");

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

  handleFilter = (values) => {
    const shortDate = this.state.startDate.toISOString().substring(0, 10);

    const choosenLocation =
      values.location === undefined ? "" : values.location;

    localStorage.setItem("filterDateWhoIsJoining", shortDate);
    localStorage.setItem("filterLocationWhoIsJoining", choosenLocation);
    this.props.filter(shortDate, values.location);
  };

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
            handleFilter={this.handleFilter}
            handleDateChange={this.handleDateChange}
            state={this.state}
            locations={this.props.locations}
          />
        </div>
      </div>
    );
  }
}

export default WhoIsJoining;
