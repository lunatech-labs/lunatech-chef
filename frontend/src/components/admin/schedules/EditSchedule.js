import React, { useState } from "react";
import { Form, Field } from "react-final-form";
import DatePicker from "react-datepicker";
import { useNavigate, useLocation } from "react-router-dom";

export default function EditSchedule(props) {
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

  function RenderData({ isRecurrent }) {
    return (
      <Form
        onSubmit={onSubmit}
        initialValues={{
          menuUuid: schedule.menu.uuid,
          locationUuid: schedule.location.uuid,
          date: date,
          recurrency: isRecurrent ? schedule.repetitionDays : "0",
        }}
        render={({ handleSubmit, submitting }) => (
          <form onSubmit={handleSubmit}>
            <div>
              <label>Choose the menu:</label>
              <Field validate={required} name="menuUuid" component="select">
                <option value="" />
                {props.menus.map((menu, index, arr) => {
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
                {props.locations.map((location, index, arr) => {
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

  const required = (value) => (value ? undefined : "Required");

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

  const schedule = useLocation().state;

  const [date, setDate] = useState(new Date(getInitialDate(schedule)));

  const handleChange = (date) => {
    setDate(date)
  };

  const navigate = useNavigate();
  const onSubmit = (values) => {
    let shortDate = date.toISOString().substring(0, 10);
    let editedSchedule = {
      ...values,
      uuid: schedule.uuid,
      date: shortDate,
    };
    // filterDate to refresh fetchSchedules
    props.editSchedule(editedSchedule);
    navigate("/allschedules");
  };

  return (
    <div className="container">
      <div>
        <h3 className="mt-4">Editing Schedule</h3>
      </div>
      <RenderData
        isRecurrent={"repetitionDays" in schedule ? true : false}
      />
      <ShowError error={props.error} />
    </div>
  );
};
