import React from "react";

const Dishes = (props) => {
  function RenderProps({ error, dishes }) {
    if (error) {
      return <h4>An error ocurred: {error}</h4>;
    } else {
      return dishes.map((dish) => (
        <div className="col-12 col-md-5 m-1" key={dish.uuid}>
          <div>Name: {dish.name}</div>
        </div>
      ));
    }
  }

  return (
    <div className="container">
      <div>
        <h3 className="mt-4">Dishes</h3>
      </div>
      <div className="col-12 col-md m-1">
        <RenderProps error={props.error} dishes={props.dishes} />
      </div>
    </div>
  );
};

export default Dishes;
