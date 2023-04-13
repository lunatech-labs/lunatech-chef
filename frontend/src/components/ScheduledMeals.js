import React from "react";
import Table from "react-bootstrap/Table";
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Button from 'react-bootstrap/Button';
import { Loading } from "./shared/Loading";
import { Form, Field } from "react-final-form";
import { ToMonth } from "./shared/Functions";

export const MealsAttendance = (props) => {
  const [attendance, setAttendance] = React.useState(props.attendance);
  const savedOffice = localStorage.getItem("filterOfficeScheduledMeals");

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
    const chosenOffice =
      values.office === undefined ? "" : values.office;

    localStorage.setItem("filterOfficeScheduledMeals", chosenOffice);
    props.filter(values.office);
  };

  function RenderData({ isLoading, error, attendance, offices }) {
    if (isLoading) {
      return (
        <Container>
          <Row>
            <Loading />
          </Row>
        </Container>
      );
    } else if (error) {
      return (
        <div>
          <h4>
            An error occurred when fetching scheduled meals from server: {error}
          </h4>
        </div>
      );
    } else {
      return (
        <div className="container">
          <Row>
            <Form
              onSubmit={handleFilter}
              initialValues={{
                office: savedOffice,
              }}
              render={({ handleSubmit, submitting }) => (

                <form onSubmit={handleSubmit}>
                  <Row>
                    <Col lg="2">
                      <label>Office:</label>
                    </Col>
                    <Col lg="3">
                      <div className="select">
                        <Field name="office" component="select" md="auto">
                          <option value="" key="" />
                          {offices.map((office) => {
                            return (
                              <option value={office.uuid} key={office.uuid}>
                                {office.city}, {office.country}
                              </option>
                            );
                          })}
                        </Field>
                      </div>
                    </Col>
                  </Row>
                  <Row>
                    <Col lg="5">
                      <div className="d-grid">
                        <Button variant="info" type="submit" disabled={submitting}>
                          Filter
                        </Button>
                      </div>
                    </Col>
                  </Row>
                </form>
              )}
            ></Form>
          </Row>
          <Row>
            <Table striped bordered hover>
              <thead>
                <tr>
                  <th>Attending?</th>
                  <th>Office</th>
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
                      <td>{attendance.office.city}</td>
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
          </Row>
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
          offices={props.offices}
        />
      </div>
    </div>
  );
};
