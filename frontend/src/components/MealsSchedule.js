import React from "react";
import { Table } from "react-bootstrap";
import { Loading } from "./shared/Loading";
import { Form, Field } from "react-final-form";
import { ToMonth } from "./shared/Functions";

export const MealsAttendance = (props) => {
  const [attendance, setAttendance] = React.useState(props.attendance);
  const savedLocation = localStorage.getItem("filterLocationMealSchedule");

  const onSubmit = (values) => {
    const newAttendance = attendance.map((item) => {
      if (item.uuid === values.uuid) {
        const updatedItem = {
          ...item,
          isAttending: values.isAttending,
        };
        return updatedItem;
      }
      return item;
    });

    setAttendance(newAttendance);
    props.editAttendance(values);
    props.showNewAttendance(newAttendance);
  };

  const handleFilter = (values) => {
    const choosenLocation =
      values.location === undefined ? "" : values.location;

    localStorage.setItem("filterLocationMealSchedule", choosenLocation);
    props.filter(values.location);
  };

  function RenderData({ isLoading, error, attendance, locations }) {
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
          <h4>
            An error ocurred when fetching meal schedules from server: {error}
          </h4>
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
                location: savedLocation,
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
                  <th>Attending?</th>
                  <th>Location</th>
                  <th>Date</th>
                  <th>Menu</th>
                </tr>
              </thead>
              <tbody>
                {attendance.map((attendance) => {
                  return (
                    <tr key={attendance.uuid}>
                      <td>
                        <Form
                          onSubmit={onSubmit}
                          initialValues={{
                            uuid: attendance.uuid,
                            isAttending: attendance.isAttending,
                          }}
                          render={({ handleSubmit }) => (
                            <form>
                              <Field name="isAttending" type="checkbox">
                                {({ input }) => (
                                  <div>
                                    <input
                                      {...input}
                                      type="checkbox"
                                      onChange={(e) => {
                                        input.onChange(e);
                                        handleSubmit();
                                      }}
                                    />
                                  </div>
                                )}
                              </Field>
                            </form>
                          )}
                        ></Form>
                      </td>
                      <td>{attendance.location.city}</td>
                      <td>
                        {attendance.date[2]} {ToMonth(attendance.date[1])}{" "}
                        {attendance.date[0]}
                      </td>
                      <td>
                        {attendance.menu.dishes.map((dish) => {
                          return (
                            <p key={dish.uuid}>
                              {dish.name}{" "}
                              {dish.description ? "- " + dish.description : ""}{" "}
                            </p>
                          );
                        })}
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

  return (
    <div className="container">
      <div>
        <h3 className="mt-4">Meals scheduled for the Lunatechies</h3>
      </div>
      <div>
        <RenderData
          isLoading={props.isLoading}
          errorListing={props.errorListing}
          attendance={attendance}
          locations={props.locations}
        />
      </div>
    </div>
  );
};
