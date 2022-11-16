import React, { Component } from "react";
import { Form, Field } from "react-final-form";
import DatePicker from "react-datepicker";

class AddSchedule extends Component {
  constructor(props) {
    super();
    this.handleChange = this.handleChange.bind(this);

    this.state = {
      startDate: new Date(),
    };
  }

  handleChange = (date) => {
    this.setState({
      startDate: date,
    });
  };

  render() {
    const required = (value) => (value ? undefined : "Required");
    const onSubmit = (values) => {
      let shortDate = this.state.startDate.toISOString().substring(0, 10);
      this.props.addNewSchedule({
        ...values,
        date: shortDate,
      });
      this.props.history.push("/allschedules");
    };

    return (
      <div className="container">
        <div>
          <h3 className="mt-4">New Schedule</h3>
        </div>
        <Form
          onSubmit={onSubmit}
          initialValues={{
            menuUuid: "",
            locationUuid: "",
            date: "", // not used. this.state.startDate used instead
            recurrency: "0",
          }}
          render={({ handleSubmit, submitting, pristine }) => (
            <form onSubmit={handleSubmit}>
              <div>
                <label>Choose the menu:</label>
                <Field validate={required} name="menuUuid" component="select">
                  <option value="" />
                  {this.props.menus.map((menu) => {
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
                <Field
                  validate={required}
                  name="locationUuid"
                  component="select"
                >
                  <option value="" />
                  {this.props.locations.map((location) => {
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
                        selected={this.state.startDate}
                        onChange={this.handleChange}
                        dateFormat="dd-MM-yyyy"
                      />
                      {meta.error && meta.touched && <span>{meta.error}</span>}
                    </div>
                  )}
                </Field>
              </div>
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
              <div>
                <button
                  type="submit"
                  color="primary"
                  disabled={submitting || pristine}
                >
                  Add Schedule
                </button>
              </div>
            </form>
          )}
        ></Form>
      </div>
    );
  }
}

export default AddSchedule;
