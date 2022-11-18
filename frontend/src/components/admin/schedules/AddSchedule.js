import React, { useState } from "react";
import { Form, Field } from "react-final-form";
import DatePicker from "react-datepicker";
import { useNavigate } from "react-router-dom";

export default function AddSchedule(props) {
  function RenderData() {
    const [date, setDate] = useState(new Date());

    const handleChange = (date) => {
      setDate(date)
    };

    const navigate = useNavigate();
    const required = (value) => (value ? undefined : "Required");

    const onSubmit = (values) => {
      let shortDate = date.toISOString().substring(0, 10);
      props.addNewSchedule({
        ...values,
        date: shortDate,
      });
      navigate("/allschedules");
    };

    return (
      <Form
        onSubmit={onSubmit}
        initialValues={{
          menuUuid: "",
          locationUuid: "",
          date: "", // not used, hook date used instead
          recurrency: "0",
        }}
        render={({ handleSubmit, submitting, pristine }) => (
          <form onSubmit={handleSubmit}>
            <div>
              <label>Choose the menu:</label>
              <Field validate={required} name="menuUuid" component="select">
                <option value="" />
                {props.menus.map((menu) => {
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
                {props.locations.map((location) => {
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
                {({ meta }) => (
                  <div>
                    <label>Choose the date:</label>
                    <DatePicker
                      selected={date}
                      onChange={handleChange}
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
    );
  }

  return (
    <div className="container">
      <div>
        <h3 className="mt-4">New Schedule</h3>
      </div>
      <RenderData />
    </div>
  );
};
