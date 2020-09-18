import React from "react";
import { Table } from "react-bootstrap";
import { Loading } from "./shared/Loading";
import { Form, Field } from "react-final-form";
import { ToMonth } from "./shared/Functions";

export const MealsSchedule = (props) => {
  const onSubmit = (values) => {
    props.saveScheduleAttendance(values);
  };

  function RenderData({ isLoading, error, schedules }) {
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
            An error ocurred when feching meal schedules from server: {error}
          </h4>
        </div>
      );
    } else {
      return (
        <div className="container">
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
                {schedules.map((schedule) => {
                  return (
                    <tr key={schedule.uuid}>
                      <td>
                        <Form
                          onSubmit={onSubmit}
                          initialValues={{
                            scheduleUuid: schedule.uuid,
                            userUuid: "",
                            isAttending: false,
                          }}
                          render={({ handleSubmit }) => (
                            <form>
                              <Field name="isAttending" component="input">
                                {({ input }) => (
                                  <div>
                                    <input
                                      {...input}
                                      type="checkbox"
                                      onChange={handleSubmit}
                                    />
                                  </div>
                                )}
                              </Field>
                            </form>
                          )}
                        ></Form>
                      </td>
                      <td>
                        {schedule.location.city}, {schedule.location.country}
                      </td>
                      <td>
                        {schedule.date[2]} {ToMonth(schedule.date[1])}{" "}
                        {schedule.date[0]}
                      </td>
                      <td>
                        {schedule.menu.dishes.map((dish) => {
                          return (
                            <p>
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
          error={props.errorListing}
          schedules={props.schedules}
        />
      </div>
    </div>
  );
};
