import { HttpErrorResponse, HttpEvent, HttpEventType } from '@angular/common/http';
import { AfterViewInit, ChangeDetectorRef, Component, ViewChild } from '@angular/core';
import { saveAs } from 'file-saver';
import { FileService } from './file.service';
import { MAT_RADIO_DEFAULT_OPTIONS } from '@angular/material/radio';
import { GitModel } from './gitRepo.model';
import { DomSanitizer } from '@angular/platform-browser';
import { MatPaginator, MatPaginatorIntl } from '@angular/material/paginator';
import { BugFile } from './bug-files.model';
import { MatTableDataSource } from '@angular/material/table';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  providers: [{
    provide: MAT_RADIO_DEFAULT_OPTIONS,
    useValue: { color: 'primary' },
}]
})
export class AppComponent implements AfterViewInit{
  filenames: string[] = [];
  fileStatus = { status: '', requestType: '', percent: 0 };
  gitRepoSelected: boolean = false;
  gitIssueGenerate: boolean = false;
  folderLocationSelected: boolean = true;
  bugReportReceived = false;
  getFilesFromServer: boolean = false;
  gitRepoLocation: string = "";
  formData: FormData = new FormData();
  processStarted: boolean = false;
  values: number[] = [5, 10, 15, 20];
  noOfBugyFiles: number = 10;
  gitLocalFileLocation: string = "";
  completeCloneRepo: boolean = false;
  gitIssueModel: GitModel = new GitModel();
  fileUrl: any;
  fileName: string = "";
  gitIssueLink: string = "";
  content: string = "";
  
  constructor(private fileService: FileService, private sanitizer: DomSanitizer) {}

  displayedColumns: string[] = ['no', 'fileName'];
  dataSource: any;

  @ViewChild(MatPaginator, { static: false }) paginator: MatPaginator = new MatPaginator(new MatPaginatorIntl(), ChangeDetectorRef.prototype);

  ngAfterViewInit() {
    // this.dataSource.paginator = this.paginator;
  }

  noOfBuggyFiles(questions: number){
    this.noOfBugyFiles = questions;
    console.log(this.noOfBugyFiles);
    
    this.formData.append('noOfBuggyFiles', this.noOfBugyFiles.toString());
    console.log(this.formData);
    
    this.noOfBugyFiles = questions;
    console.log(this.noOfBugyFiles);
  }

  isGitRipoSelected(): void {
    this.gitRepoSelected = true;
    this.folderLocationSelected = false;
    this.gitIssueGenerate = false;
    this.bugReportReceived = false;
    console.log(this.gitRepoSelected);
  }
  saveChanges(): void {
    this.processStarted = true;
    console.log("sami");
    this.formData.append('gitRepoLink', this.gitRepoLocation);
    this.fileService.cloneGitRepo(this.formData).subscribe((gitRepoLocation: string) => {
      this.completeCloneRepo = true;
      this.processStarted = false;
      console.log(gitRepoLocation);
      this.gitLocalFileLocation = gitRepoLocation.toString();
    })
  }

  isFolderLocationSelected(): void {
    this.folderLocationSelected = true;
    this.gitIssueGenerate = false;
    this.gitRepoSelected = false;
    this.bugReportReceived = false;
    this.completeCloneRepo = false;
  }

  // define a function to upload files
  onUploadFiles(files: File[]): void {
    var i = 0;
    console.log(files);
    for (const file of files) { 
      if(file.name.endsWith('.java')) {
        this.formData.append('files', file, file.name); 
        console.log(i + " " + file.name);
        i++;
      }
    }
    // console.log(this.formData);
    
  }

  // define a function to upload files
  onUploadBugReport(event: any): void {
    const file:File = event.target.files[0];
    console.log(file.name);
    this.formData.append('bugReport', file, file.name);
  }

  localizeBug(): void {
    var listBugFiles: BugFile[] = [];
    this.processStarted = true;
    console.log("bug localization button selected");
    this.fileService.upload(this.formData).subscribe(
      files => {
        this.processStarted = false;
        this.getFilesFromServer = true;
        console.log(files);
        for(var i = 0; i < files.length; i++) {
          console.log(files[i]);
          listBugFiles[i] = {'fileName': files[i], 'no': i+1}
          // this.listBugFiles[i].fileName = files[i];
          // this.listBugFiles[i].no = i + 1;
          this.dataSource = new MatTableDataSource(listBugFiles);
          this.dataSource.paginator = this.paginator;
          console.log(listBugFiles);
          // this.formData = new FormData();
        }
      },
      (error: HttpErrorResponse) => {
        console.log(error);
      }
    ); 
    this.formData = new FormData();
  }

  getBugReport(): void {
    this.gitIssueGenerate = true;
    this.gitRepoSelected = false;
    this.folderLocationSelected = false;
    this.completeCloneRepo = false;
  }

  downloadBugReport(): void {
    this.gitIssueLink = this.gitIssueLink.replace("https://github.com/", "https://api.github.com/repos/");
    this.fileService.getIssueDescription(this.gitIssueLink).subscribe(
      event => {
        this.bugReportReceived = true;
        this.gitIssueModel = event;
        this.content = this.gitIssueModel.title + '\n' + this.gitIssueModel.body;
        console.log(this.content);
        const blob = new Blob([this.content], { type: 'application/octet-stream' });
        this.fileName = this.gitIssueModel.number.toString() + ".txt";
        this.fileUrl = this.sanitizer.bypassSecurityTrustResourceUrl(window.URL.createObjectURL(blob));
      }
    )
  }

  // define a function to download files
  onDownloadFile(filename: string): void {
    this.fileService.download(filename).subscribe(
      event => {
        console.log(event);
        this.resportProgress(event);
      },
      (error: HttpErrorResponse) => {
        console.log(error);
      }
    );
  }

  private resportProgress(httpEvent: HttpEvent<string[] | Blob>): void {
    switch(httpEvent.type) {
      case HttpEventType.UploadProgress:
        this.updateStatus(httpEvent.loaded, httpEvent.total!, 'Uploading... ');
        break;
      case HttpEventType.DownloadProgress:
        this.updateStatus(httpEvent.loaded, httpEvent.total!, 'Downloading... ');
        break;
      case HttpEventType.ResponseHeader:
        console.log('Header returned', httpEvent);
        break;
      case HttpEventType.Response:
        if (httpEvent.body instanceof Array) {
          this.fileStatus.status = 'done';
          for (const filename of httpEvent.body) {
            this.filenames.unshift(filename);
          }
        } else {
          saveAs(new File([httpEvent.body!], httpEvent.headers.get('File-Name')!, 
                  {type: `${httpEvent.headers.get('Content-Type')};charset=utf-8`}));
          // saveAs(new Blob([httpEvent.body!], 
          //   { type: `${httpEvent.headers.get('Content-Type')};charset=utf-8`}),
          //    httpEvent.headers.get('File-Name'));
        }
        this.fileStatus.status = 'done';
        break;
        default:
          console.log(httpEvent);
          break;
      
    }
  }

  private updateStatus(loaded: number, total: number, requestType: string): void {
    this.fileStatus.status = 'progress';
    this.fileStatus.requestType = requestType;
    this.fileStatus.percent = Math.round(100 * loaded / total);
  }
}
