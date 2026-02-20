import React from "react";
import Table from "react-bootstrap/Table";
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Button from 'react-bootstrap/Button';
import Alert from 'react-bootstrap/Alert';
import { Loading } from "./shared/Loading";
import { Form, Field } from "react-final-form";
import { ToMonth } from "./shared/Functions";
import { STORAGE_FILTER_OFFICE_MEALS } from "../redux/LocalStorageKeys";

export const ListMealsForUser = (props) => {
  const [attendance, setAttendance] = React.useState(props.attendance);
  const savedOffice = localStorage.getItem(STORAGE_FILTER_OFFICE_MEALS);

  React.useEffect(() => {
    setAttendance(props.attendance);
  }, [props.attendance]);

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

    localStorage.setItem(STORAGE_FILTER_OFFICE_MEALS, chosenOffice);
    props.filter();
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
        <Alert key="danger" variant="danger">
          An error occurred when fetching scheduled meals from server: {error}
        </Alert>
      );
    } else {
      return (
        <div className="container">
          <Row>
            <div className="shadow-sm p-3 mb-5 bg-white rounded">
              <Form
                onSubmit={handleFilter}
                initialValues={{
                  office: savedOffice,
                }}
                render={({ handleSubmit, submitting }) => (

                  <form onSubmit={handleSubmit}>
                    <Row>
                      <Col lg="1">
                        <label>Office:</label>
                      </Col>
                      <Col lg="3">
                        <div className="select">
                          <Field name="office" component="select" md="auto">
                            <option value="" key="" />
                            {offices.map((office) => {
                              return (
                                <option value={office.uuid} key={office.uuid}>
                                  {office.city}
                                </option>
                              );
                            })}
                          </Field>
                        </div>
                      </Col>
                    </Row>
                    <Row>
                      <Col lg="4">
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
            </div>
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
                {attendance.map((item) => {
                  return (
                    <tr key={item.uuid}>
                      <td>
                        <Form
                          onSubmit={onSubmit}
                          initialValues={{
                            uuid: item.uuid,
                            isAttending: item.isAttending,
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
                      <td>{item.office}</td>
                      <td>
                        {item.date[2]} {ToMonth(item.date[1])}{" "}
                        {item.date[0]}
                      </td>
                      <td>{item.menu.name} {item.menu.dishes.length > 0 ? (":") : ("")}
                        {item.menu.dishes.map((dish) => {
                          return (
                            <p style={{ margin: 5, padding: 0 }} key={dish.uuid}>• {dish.name}</p>
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
        <h3 className="mt-4">Lunches I'm attending</h3>
      </div>
      <div>
        <RenderData
          isLoading={props.isLoading}
          error={props.errorListing}
          attendance={attendance}
          offices={props.offices}
        />
      </div>
    </div>
  );
};
