import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
 
  baseUrl = 'http://localhost:8080/api/v1/chat';
  constructor(private http: HttpClient) { }
     

    getResponse(inputText: string): Observable<String>{
         return this.http.get(`${this.baseUrl}?inputText=${inputText}`,{responseType: 'text'});
    }
}

