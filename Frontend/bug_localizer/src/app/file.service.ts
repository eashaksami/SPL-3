import { HttpClient, HttpEvent } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { GitModel } from './gitRepo.model';
import { Issue } from './issues.model';

@Injectable({providedIn: 'root'})
export class FileService {
  private server = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  // define function to upload files
  upload(formData: FormData): Observable<string[]> {
    return this.http.post<string[]>(`${this.server}/file/upload`, formData).pipe(map((response => <string[]>response)));
  }

  cloneGitRepo(formData: FormData): Observable<string> {
    return this.http.post<string>(`${this.server}/file/clone-repo/`,formData,{ responseType: 'text' as 'json'  }).pipe(map((response => <string>response)));
  }

  getIssueDescription(gitIssueLink: string): Observable<GitModel> {
    // return this.http.get<GitModel>('https://api.github.com/repos/spring-projects/spring-boot/issues/33347').pipe(map((response => <GitModel>response)));
    return this.http.get<GitModel>(gitIssueLink).pipe(map((response => <GitModel>response)));
  }

  getAllIssue(): Observable<Issue> {
    // return this.http.get<GitModel>('https://api.github.com/repos/spring-projects/spring-boot/issues/33347').pipe(map((response => <GitModel>response)));
    return this.http.get<Issue>('https://api.github.com/search/issues?q=repo:spring-projects/spring-boot type:issue state:open').pipe(map((response => <Issue>response)));
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      console.error(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }

  // define function to download files
  download(filename: string): Observable<HttpEvent<Blob>> {
    return this.http.get(`${this.server}/file/download/${filename}/`, {
      reportProgress: true,
      observe: 'events',
      responseType: 'blob'
    });
  }
  
}
