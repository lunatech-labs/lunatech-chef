import React, { Component } from "react";
import { Link, withRouter } from "react-router-dom";
import Table from "react-bootstrap/Table";
import Button from "react-bootstrap/Button";
import { Loading } from "../../shared/Loading";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faMinus, faPlus, faEdit } from "@fortawesome/free-solid-svg-icons";

function ShowError({ error, reason }) {
  if (error) {
    return (
      <div>
        <h4>
          An error ocurred when {reason} a location: {error}
        </h4>
      </div>
    );
  } else {
    return <div></div>;
  }
}

function RenderData({ isLoading, error, locations, handleEdit, handleRemove }) {
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
        <h4>An error ocurred when feching Locations from server: {error}</h4>
      </div>
    );
  } else {
    return (
      <div className="container">
        <div className="row">
          <Table striped bordered hover>
            <thead>
              <tr>
                <th>City</th>
                <th>Country</th>
                <th></th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {locations.map((loc) => {
                return (
                  <tr key={loc.uuid}>
                    <td>{loc.city}</td>
                    <td>{loc.country}</td>
                    <td>
                      <Button
                        variant="primary"
                        value={loc.uuid}
                        onClick={() => handleEdit(loc)}
                      >
                        <FontAwesomeIcon icon={faEdit} />
                      </Button>
                    </td>
                    <td>
                      <Button
                        variant="danger"
                        value={loc.uuid}
                        onClick={() => handleRemove(loc.uuid)}
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

class ListLocations extends Component {
  constructor(props) {
    super(props);
    this.handleRemove = this.handleRemove.bind(this);
    this.handleEdit = this.handleEdit.bind(this);
  }

  handleRemove(uuid) {
    this.props.deleteLocation(uuid);
  }

  handleEdit(location) {
    this.props.history.push("/editlocation", location);
  }

  render() {
    return (
      <div className="container">
        <div>
          <h3 className="mt-4">Management of Locations</h3>
        </div>
        <Link to={`/newlocation`}>
          <button type="button" className="btn btn-success">
            <i>
              <FontAwesomeIcon icon={faPlus} />
            </i>{" "}
            New Location
          </button>
        </Link>
        <div>
          <RenderData
            isLoading={this.props.isLoading}
            error={this.props.errorListing}
            locations={this.props.locations}
            handleEdit={this.handleEdit}
            handleRemove={this.handleRemove}
          />
          <ShowError error={this.props.errorAdding} reason="adding" />
          <ShowError error={this.props.errorDeleting} reason="deleting" />
          <ShowError error={this.props.errorEditing} reason="saving" />
        </div>
      </div>
    );
  }
}

export default withRouter(ListLocations);
