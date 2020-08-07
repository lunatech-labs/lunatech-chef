import React, { Component } from "react";
import { withRouter } from "react-router-dom";
import { Form, Field } from "react-final-form";
import DatePicker from "react-datepicker";

function ShowError({ error }) {
  if (error) {
    return (
      <div>
        <h4>An error ocurred when adding new Schedule: {error}</h4>
      </div>
    );
  } else {
    return <div></div>;
  }
}

class AddSchedule extends Component {
  constructor(props) {
    super(props);
    this.handleChange = this.handleChange.bind(this);
  }

  state = {
    startDate: new Date(),
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
            date: "",
          }}
          render={({ handleSubmit, submitting, pristine }) => (
            <form onSubmit={handleSubmit}>
              <div>
                <label>Choose the menu:</label>
                <Field validate={required} name="menuUuid" component="select">
                  <option value="" />
                  {this.props.menus.map((menu, index, arr) => {
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
                  {this.props.locations.map((location, index, arr) => {
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
        <ShowError error={this.props.error} />
      </div>
    );
  }
}

export default withRouter(AddSchedule);
