import React from "react";
import { Link, withRouter } from "react-router-dom";
import { Table, Button } from "react-bootstrap";
import { Loading } from "../../shared/Loading";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faMinus, faPlus, faEdit } from "@fortawesome/free-solid-svg-icons";
import { ToMonth } from "../../shared/Functions";
import DatePicker from "react-datepicker";

export const ListSchedules = (props) => {
  const formatedDate = new Date(props.fromDate);
  const [fromNewDate, setDate] = React.useState(formatedDate);

  const handleEdit = (schedule) => {
    props.history.push("/editschedule", schedule);
  };

  const handleRemove = (uuid) => {
    // props.fromDate to refresh fetchSchedules
    props.deleteSchedule(uuid, props.fromDate);
  };

  const handleDateChange = (date) => {
    let shortDate = date.toISOString().substring(0, 10);
    setDate(date);
    props.filterDates(shortDate);
  };

  function ShowError({ error, reason }) {
    if (error) {
      return (
        <h4>
          An error ocurred when {reason} a schedule: {error}
        </h4>
      );
    } else {
      return <div></div>;
    }
  }

  function RenderData({
    isLoading,
    error,
    schedules,
    handleEdit,
    handleRemove,
    fromDate,
    handleDateChange,
  }) {
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
          <h4>An error ocurred when fetching Schedules from server: {error}</h4>
        </div>
      );
    } else {
      return (
        <div className="container">
          <div className="row">
            <form>
              <div>
                <div>
                  <label>Filter from: </label>
                  <DatePicker
                    selected={fromDate}
                    onChange={handleDateChange}
                    dateFormat="dd-MM-yyyy"
                  />
                </div>
              </div>
            </form>
          </div>
          <div className="row">
            <Table striped bordered hover>
              <thead>
                <tr>
                  <th>Menu</th>
                  <th>Location</th>
                  <th>Date</th>
                  <th></th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {schedules.map((schedule) => {
                  return (
                    <tr key={schedule.uuid}>
                      <td>{schedule.menu.name}</td>
                      <td>
                        {schedule.location.city}, {schedule.location.country}
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
                          onClick={() => handleRemove(schedule.uuid)}
                        >
                          <FontAwesomeIcon icon={faMinus} />
                        </Button>
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
        <h3 className="mt-4">Management of Schedules</h3>
      </div>
      <Link to={`/newSchedule`}>
        <button type="button" className="btn btn-success">
          <i>
            <FontAwesomeIcon icon={faPlus} />
          </i>{" "}
          New Schedule
        </button>
      </Link>
      <div>
        <RenderData
          isLoading={props.isLoading}
          error={props.errorListing}
          schedules={props.schedules}
          handleEdit={handleEdit}
          handleRemove={handleRemove}
          fromDate={fromNewDate}
          handleDateChange={handleDateChange}
        />
        <ShowError error={props.errorAdding} reason="adding" />
        <ShowError error={props.errorDeleting} reason="deleting" />
        <ShowError error={props.errorEditing} reason="saving" />
      </div>
    </div>
  );
};

export default withRouter(ListSchedules);
