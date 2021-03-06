import React, { Component } from "react";
import { Loading } from "../../shared/Loading";
import { Link, withRouter } from "react-router-dom";
import { Table, Button } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faMinus,
  faPlus,
  faEdit,
  faCheck,
} from "@fortawesome/free-solid-svg-icons";

function ShowError({ error, reason }) {
  if (error) {
    return (
      <h4>
        An error ocurred when {reason} a dish: {error}
      </h4>
    );
  } else {
    return <div></div>;
  }
}

function RenderData({ isLoading, error, dishes, handleEdit, handleRemove }) {
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
        <h4>An error ocurred when feching Dishes from server: {error}</h4>
      </div>
    );
  } else {
    return (
      <div className="container">
        <div className="row">
          <Table striped bordered hover>
            <thead>
              <tr>
                <th>Name</th>
                <th>Description</th>
                <th>Vegetarian</th>
                <th>Nuts</th>
                <th>Seafood</th>
                <th>Pork</th>
                <th>Beef</th>
                <th>GlutenFree</th>
                <th>Lactose</th>
                <th></th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {dishes.map((dish) => {
                return (
                  <tr key={dish.uuid}>
                    <td>{dish.name}</td>
                    <td>{dish.description}</td>
                    <td>
                      {dish.isVegetarian ? (
                        <span>
                          <FontAwesomeIcon icon={faCheck} />
                        </span>
                      ) : (
                        ""
                      )}
                    </td>
                    <td>
                      {dish.hasNuts ? (
                        <span>
                          <FontAwesomeIcon icon={faCheck} />
                        </span>
                      ) : (
                        ""
                      )}
                    </td>
                    <td>
                      {dish.hasSeafood ? (
                        <span>
                          <FontAwesomeIcon icon={faCheck} />
                        </span>
                      ) : (
                        ""
                      )}
                    </td>
                    <td>
                      {dish.hasPork ? (
                        <span>
                          <FontAwesomeIcon icon={faCheck} />
                        </span>
                      ) : (
                        ""
                      )}
                    </td>
                    <td>
                      {dish.hasBeef ? (
                        <span>
                          <FontAwesomeIcon icon={faCheck} />
                        </span>
                      ) : (
                        ""
                      )}
                    </td>
                    <td>
                      {dish.isGlutenFree ? (
                        <span>
                          <FontAwesomeIcon icon={faCheck} />
                        </span>
                      ) : (
                        ""
                      )}
                    </td>
                    <td>
                      {dish.hasLactose ? (
                        <span>
                          <FontAwesomeIcon icon={faCheck} />
                        </span>
                      ) : (
                        ""
                      )}
                    </td>
                    <td>
                      <Button
                        variant="primary"
                        value={dish.uuid}
                        onClick={() => handleEdit(dish)}
                      >
                        <FontAwesomeIcon icon={faEdit} />
                      </Button>
                    </td>
                    <td>
                      <Button
                        variant="danger"
                        value={dish.uuid}
                        onClick={() => handleRemove(dish.uuid)}
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

class ListDishes extends Component {
  constructor(props) {
    super();
    this.handleRemove = this.handleRemove.bind(this);
    this.handleEdit = this.handleEdit.bind(this);
  }

  handleRemove(uuid) {
    this.props.deleteDish(uuid);
  }

  handleEdit(dish) {
    this.props.history.push("/editdish", dish);
  }

  render() {
    return (
      <div className="container">
        <div>
          <h3 className="mt-4">Management of Dishes</h3>
        </div>
        <Link to={`/newdish`}>
          <button type="button" className="btn btn-success">
            <i>
              <FontAwesomeIcon icon={faPlus} />
            </i>{" "}
            New Dish
          </button>
        </Link>
        <div>
          <RenderData
            isLoading={this.props.isLoading}
            error={this.props.errorListing}
            dishes={this.props.dishes}
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

export default withRouter(ListDishes);
