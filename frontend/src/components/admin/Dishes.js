import React from "react";
import ListPresentation from "../shared/ListPresentation";

const Dishes = (props) => {
  return (
    <div className="container">
      <div>
        <h3 className="mt-4">Dishes</h3>
      </div>
      <div className="col-12 col-md m-1">
        <ListPresentation
          isLoading={props.isLoading}
          error={props.error}
          data={props.dishes}
        />
      </div>
    </div>
  );
};

export default Dishes;
