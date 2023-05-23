import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Container from 'react-bootstrap/Container';
import Table from "react-bootstrap/Table";
import Button from "react-bootstrap/Button";
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Alert from 'react-bootstrap/Alert';
import { Loading } from "../../shared/Loading";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faMinus, faPlus, faEdit } from "@fortawesome/free-solid-svg-icons";
import { ToMonth } from "../../shared/Functions";
import { Form, Field } from "react-final-form";
import DatePicker from "react-datepicker";

export default function ListSchedules(props) {

  function ShowError({ error, reason }) {
    return (
      <Alert key="danger" variant="danger">
        An error occured when {reason} a schedule: {error}
      </Alert>
    );
  }

  const savedDate = localStorage.getItem("filterDateSchedule");
  const savedOfficeSchedule = localStorage.getItem(
    "filterOfficeSchedule"
  );

  const [startDateSchedule, setDateSchedule] = useState(savedDate === null ? new Date() : new Date(savedDate));
  const [startOfficeSchedule, _] = useState(savedOfficeSchedule === null ? "" : savedOfficeSchedule);

  const handleDateChange = (date) => {
    setDateSchedule(date)
  };

  const navigate = useNavigate();
  const handleEdit = (schedule) => {
    navigate("/editschedule", { state: schedule });
  }

  const handleRemoveSchedule = (uuid) => {
    props.deleteSchedule(uuid);
  };

  const handleRemoveRecurrentSchedule = (uuid) => {
    props.deleteRecurrentSchedule(uuid);
  };

  const handleFilter = (values) => {
    const shortDate = startDateSchedule
      .toISOString()
      .substring(0, 10);

    const chosenOffice =
      values.office === undefined ? "" : values.office;

    localStorage.setItem("filterDateSchedule", shortDate);
    localStorage.setItem("filterOfficeSchedule", chosenOffice);
    props.filterSchedules();
    props.filterRecurrentSchedules();
  };

  return (
    <Container>
      <div>
        <h3 className="mt-4">Management of Scheduled Menus</h3>
      </div>
      <div>
        {props.errorAdding ? <ShowError error={props.errorAdding} reason="adding" /> : <div></div>}
        {props.errorDeleting ? <ShowError error={props.errorDeleting} reason="deleting" /> : <div></div>}
        {props.errorEditing ? <ShowError error={props.errorEditing} reason="saving" /> : <div></div>}
        {props.isLoading ? (
          <Row>
            <Loading />
          </Row>
        ) : (
          <div></div>
        )}
        {props.errorListing ? (
          <Alert key="danger" variant="danger">
            An error occurred when feching Schedules from server:  {props.errorListing}
          </Alert>
        ) : (
          <div>

            <Row>
              <div className="shadow-sm p-3 mb-5 bg-white rounded">
                <Form
                  onSubmit={handleFilter}
                  initialValues={{
                    office: startOfficeSchedule,
                    date: "", // not used, startDateSchedule used instead
                  }}
                  render={({ handleSubmit, submitting }) => (
                    <form onSubmit={handleSubmit}>
                      <Row>
                        <Col lg="1">Office:
                        </Col>
                        <Col lg="3">
                          <div className="select">
                            <Field name="office" component="select" md="auto">
                              <option value="" key="" />
                              {props.offices.map((office) => {
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
                        <Col lg="1">Date:
                        </Col>
                        <Col lg="3">
                          <Field name="date" component="input">
                            {({ meta }) => (
                              <div className="datePicker">
                                <DatePicker
                                  selected={startDateSchedule}
                                  onChange={handleDateChange}
                                  dateFormat="dd-MM-yyyy"
                                />
                                {meta.error && meta.touched && (
                                  <span className="text-danger">  {meta.error}</span>
                                )}
                              </div>
                            )}
                          </Field>
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
                        <Col>
                          <Link to={`/newSchedule`}>
                            <button type="button" className="btn btn-success">
                              <i>
                                <FontAwesomeIcon icon={faPlus} />
                              </i>{" "}
                              New Schedule
                            </button>
                          </Link>
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
                    <th>Menu</th>
                    <th>Office</th>
                    <th>Date</th>
                    <th></th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {props.schedules.map((schedule) => {
                    return (
                      <tr key={schedule.uuid}>
                        <td>{schedule.menu.name}</td>
                        <td>
                          {schedule.office.city}
                        </td>
                        <td>
                          {schedule.date[2]} {ToMonth(schedule.date[1])}{" "}
                          {schedule.date[0]}
                        </td>
                        <td>
                          <Button
                            variant="primary"
                            value={schedule.uuid}
                            onClick={() => handleEdit(schedule)}
                          >
                            <FontAwesomeIcon icon={faEdit} />
                          </Button>
                        </td>
                        <td>
                          <Button
                            variant="danger"
                            value={schedule.uuid}
                            onClick={() => handleRemoveSchedule(schedule.uuid)
                            }
                          >
                            <FontAwesomeIcon icon={faMinus} />
                          </Button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </Table>
            </Row>
          </div>
        )}
      </div>
      <div>
        <h4 className="mt-4">Recurrent Schedules</h4>
        <h5 className="mt-4">
          List of menus to be scheduled automatically based on its recurrency
        </h5>
      </div>
      <div>
        <Row>
          <Table striped bordered hover>
            <thead>
              <tr>
                <th>Menu</th>
                <th>Office</th>
                <th>Recurrency (days)</th>
                <th>Next date</th>
                <th></th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {props.recurrentSchedules.map((recurrentSchedule) => {
                return (
                  <tr key={recurrentSchedule.uuid}>
                    <td>{recurrentSchedule.menu.name}</td>
                    <td>
                      {recurrentSchedule.office.city}
                    </td>
                    <td>{recurrentSchedule.repetitionDays}</td>
                    <td>
                      {" "}
                      {recurrentSchedule.nextDate[2]}{" "}
                      {ToMonth(recurrentSchedule.nextDate[1])}{" "}
                      {recurrentSchedule.nextDate[0]}
                    </td>
                    <td>
                      <Button
                        variant="primary"
                        value={recurrentSchedule.uuid}
                        onClick={() => handleEdit(recurrentSchedule)}
                      >
                        <FontAwesomeIcon icon={faEdit} />
                      </Button>
                    </td>
                    <td>
                      <Button
                        variant="danger"
                        value={recurrentSchedule.uuid}
                        onClick={() => handleRemoveRecurrentSchedule(recurrentSchedule.uuid)}
                      >
                        <FontAwesomeIcon icon={faMinus} />
                      </Button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </Table>
        </Row>
      </div>
    </Container>
  );
}
