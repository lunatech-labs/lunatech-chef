import React, { Component } from "react";
import { withRouter } from "react-router-dom";
import { Form, Field } from "react-final-form";
import DatePicker from "react-datepicker";

function ShowError({ error }) {
  if (error) {
    return (
      <div>
        <h4>An error ocurred when editing a Schedule: {error}</h4>
      </div>
    );
  } else {
    return <div></div>;
  }
}

function RenderData({
  required,
  onSubmit,
  handleChange,
  schedule,
  menus,
  locations,
  startDate,
}) {
  return (
    <Form
      onSubmit={onSubmit}
      initialValues={{
        menuUuid: schedule.menu.uuid,
        locationUuid: schedule.location.uuid,
        date: startDate,
      }}
      render={({ handleSubmit, submitting }) => (
        <form onSubmit={handleSubmit}>
          <div>
            <label>Choose the menu:</label>
            <Field validate={required} name="menuUuid" component="select">
              <option value="" />
              {menus.map((menu, index, arr) => {
                return (
                  <option value={menu.uuid} key={menu.uuid}>
                    {menu.name}
                  </option>
                );
              })}
            </Field>
          </div>
          <div>
            <label>Choose the location:</label>
            <Field validate={required} name="locationUuid" component="select">
              <option value="" />
              {locations.map((location, index, arr) => {
                return (
                  <option value={location.uuid} key={location.uuid}>
                    {location.city}, {location.country}
                  </option>
                );
              })}
            </Field>
          </div>
          <div>
            <Field name="date" component="input">
              {({ input, meta }) => (
                <div>
                  <label>Choose the date:</label>
                  <DatePicker
                    selected={startDate}
                    onChange={handleChange}
                    dateFormat="dd-MM-yyyy"
                  />
                  {meta.error && meta.touched && <span>{meta.error}</span>}
                </div>
              )}
            </Field>
          </div>
          <div>
            <button type="submit" color="primary" disabled={submitting}>
              Add Schedule
            </button>
          </div>
        </form>
      )}
    ></Form>
  );
}

class EditSchedule extends Component {
  constructor(props) {
    super(props);
    this.handleChange = this.handleChange.bind(this);
  }

  state = {
    startDate: new Date().setFullYear(
      this.props.schedule.date[0],
      this.props.schedule.date[1] - 1,
      this.props.schedule.date[2]
    ),
  };

  handleChange = (date) => {
    this.setState({
      startDate: date,
    });
  };

  render() {
    const required = (value) => (value ? undefined : "Required");
    const onSubmit = (values) => {
      let shortDate = this.state.startDate.toISOString().substring(0, 10);
      let editedSchedule = {
        ...values,
        uuid: this.props.schedule.uuid,
        date: shortDate,
      };
      this.props.editSchedule(editedSchedule);
      this.props.history.push("/allschedules");
    };

    return (
      <div className="container">
        <div>
          <h3 className="mt-4">Editing Schedule</h3>
        </div>
        <RenderData
          required={required}
          onSubmit={onSubmit}
          handleChange={this.handleChange}
          schedule={this.props.schedule}
          menus={this.props.menus}
          locations={this.props.locations}
          startDate={this.state.startDate}
        />
        <ShowError error={this.props.error} />
      </div>
    );
  }
}

export default withRouter(EditSchedule);
