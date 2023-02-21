import React from "react";
import styled from "styled-components";
import {useUser} from "../hook/User";
import {useForm} from "react-hook-form";
import {search} from "../backend/idm";
import {useSearchParams} from "react-router-dom";
import {useNavigate} from "react-router-dom";
const StyledDiv = styled.div`
  display: flex;
  flex-direction: column;
`

const StyledH1 = styled.h1`
`

const StyledInput = styled.input`
`

const StyledButton = styled.button`
`

const Home = () => {
    const {
        accessToken, setAccessToken,
        refreshToken, setRefreshToken
    } = useUser();


    const {register, getValues, handleSubmit} = useForm();
    const [movies, setMovies] = React.useState([]);
    const [searchParams, setSearchParams] = useSearchParams();
    const [page, setPage] = React.useState(1);
    const navigate = useNavigate();

    const pageNext = () =>{
       setPage(prevState => { submitQuery(prevState+1); return prevState+1});
       console.log("next page is " + page);
    }
    const pagePrev = () =>{
        setPage(prevState => { submitQuery(prevState-1); return prevState-1});
        console.log("next page is " + page);
    }
    const submitQuery = (myPage1) =>{
   //function submitQuery (myPage) {
        const title = getValues("title");
        const year = getValues("year");
        const director = getValues("director");
        const genre = getValues("genre");
        const limit = getValues("limit");
        const orderBy = getValues("orderBy");
        const direction = getValues("direction");
        console.log(myPage1)
        const payLoad = {
            title: title!==""?title:null,
            year: year!==""?year:null,
            director: director!==""?director:null,
            genre: genre!==""?genre:null,
            direction: direction!==""?direction:null,
            orderBy: orderBy!==""?orderBy:null,
            limit : limit!==""?limit:null,
            page: myPage1 !==0 ? myPage1:null
        }
        //setSearchParams(payLoad);
        console.log(payLoad);
        search(accessToken,payLoad)
            .then(response => setMovies(response.data.movies))
            .catch(error => alert(JSON.stringify(error.response.data, null, 2)))
        console.log("this will print right away, since the above statement is a promise and will be done asynchronous.")
    }

    return (
        <StyledDiv>
            <h1>Home</h1>
            <label> Title:
                <input  {...register("title",{required:false})} type={"text"}/> </label>
            <label> Year:<input {...register("year", {required:false})} type={"number"}/></label>
            <label>Director:<input  {...register("director", {required:false})} type={"text"}/></label>
            <label>Genre:<input {...register("genre", {required:false})} type={"text"}/></label>
            <label>Limit:<input {...register("limit", {required:false})} type={"number"}/></label>
            <label>OrderBy:<input {...register("orderBy", {required:false})} type={"text"}/></label>
            <label>Direction:<input {...register("direction", {required:false})} type={"text"}/></label>
            <button onClick={handleSubmit(()=>{setPage(1); submitQuery(1)})}>Search</button>
            <br />
            <button onClick={()=>pagePrev()}>Prev</button>
            <label> Current Page: {page} </label>
            <button onClick={()=>pageNext()}>Next</button>

            <table>
                <thead>
                <tr>
                    <th>poster</th>
                    <th>title</th>
                    <th>year</th>
                    <th>director</th>
                </tr>
                </thead>

                {
                    movies!==undefined &&
                    movies.map(movie =>
                    <tbody key={movie.id}>
                    <tr>
                        <td><img src= {"https://image.tmdb.org/t/p/original"+movie.posterPath} width="150" height="150">
                        </img></td>
                        <td>{movie.title}</td>
                        <td>{movie.year}</td>
                        <td>{movie.director}</td>
                    </tr>
                    </tbody>
                )}
            </table>
        </StyledDiv>
    );
}

export default Home;
