import React, { Component } from "react";
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
  isRecurrent,
}) {
  return (
    <Form
      onSubmit={onSubmit}
      initialValues={{
        menuUuid: schedule.menu.uuid,
        locationUuid: schedule.location.uuid,
        date: startDate,
        recurrency: isRecurrent ? schedule.repetitionDays : "0",
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
          {!isRecurrent /** Only modify date for single schedules  */ ? (
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
          ) : (
            <div></div>
          )}
          {isRecurrent /** A single schedule cannot be made recurrent  */ ? (
            <div>
              <label>Repetition:</label>
              <Field validate={required} name="recurrency" component="select">
                <option value="0" key="0">
                  Single event
                </option>
                <option value="7" key="7">
                  Every 7 days
                </option>
                <option value="14" key="14">
                  Every 14 days
                </option>
                <option value="21" key="21">
                  Every 21 days
                </option>
                <option value="28" key="28">
                  Every 28 days
                </option>
              </Field>
            </div>
          ) : (
            <div></div>
          )}
          <div>
            <button type="submit" color="primary" disabled={submitting}>
              Save Schedule
            </button>
          </div>
        </form>
      )}
    ></Form>
  );
}

const getInitialDate = (schedule) => {
  if ("date" in schedule) {
    return new Date().setFullYear(
      schedule.date[0],
      schedule.date[1] - 1,
      schedule.date[2]
    );
  } else {
    return new Date().setFullYear(
      schedule.nextDate[0],
      schedule.nextDate[1] - 1,
      schedule.nextDate[2]
    );
  }
};

class EditSchedule extends Component {
  constructor(props) {
    super(props);
    this.handleChange = this.handleChange.bind(this);
  }

  state = {
    startDate: new Date(getInitialDate(this.props.schedule)),
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
      // filterDate to refresh fetchSchedules
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
          isRecurrent={"repetitionDays" in this.props.schedule ? true : false}
        />
        <ShowError error={this.props.error} />
      </div>
    );
  }
}

export default EditSchedule;
